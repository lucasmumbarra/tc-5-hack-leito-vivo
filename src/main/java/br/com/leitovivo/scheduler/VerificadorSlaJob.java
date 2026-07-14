package br.com.leitovivo.scheduler;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.enums.DecisaoSla;
import br.com.leitovivo.domain.sla.RegraAlertaSla;
import br.com.leitovivo.domain.sla.model.SlaAplicavel;
import br.com.leitovivo.domain.sla.SlaResolver;
import br.com.leitovivo.persistence.entity.AlertaLeito;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.repository.LeitoSlaRepository;
import br.com.leitovivo.service.AlertaService;
import br.com.leitovivo.service.LeitoService;
import br.com.leitovivo.service.SlaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class VerificadorSlaJob {

    public static final String MOTIVO_TIMEOUT_HIGIENIZACAO = "TIMEOUT_HIGIENIZACAO";

    private static final Set<StatusLeito> STATUS_MONITORADOS = EnumSet.of(
            StatusLeito.OCUPADO,
            StatusLeito.EM_HIGIENIZACAO,
            StatusLeito.RESERVADO,
            StatusLeito.MANUTENCAO);

    private final SlaService slaService;
    private final AlertaService alertaService;
    private final LeitoService leitoService;
    private final LeitoSlaRepository leitoSlaRepository;
    private final Clock clock;

    public VerificadorSlaJob(
            SlaService slaService,
            AlertaService alertaService,
            LeitoService leitoService,
            LeitoSlaRepository leitoSlaRepository,
            Clock clock) {
        this.slaService = slaService;
        this.alertaService = alertaService;
        this.leitoService = leitoService;
        this.leitoSlaRepository = leitoSlaRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "America/Sao_Paulo")
    public void agendado() {
        executar(Instant.now(clock));
    }

    /**
     * Ponto de entrada testável — instante controlado pelo chamador.
     */
    @Transactional
    public void executar(Instant agora) {
        List<SlaAplicavel> regras = slaService.listarComoRegras();
        List<Leito> leitos = leitoSlaRepository.findByStatusIn(STATUS_MONITORADOS);

        for (Leito leito : leitos) {
            SlaResolver.resolver(
                            leito.getUnidade().getId(),
                            leito.getTipo(),
                            leito.getStatus(),
                            regras)
                    .ifPresent(sla -> processar(leito, sla, agora));
        }
    }

    private void processar(Leito leito, SlaAplicavel sla, Instant agora) {
        DecisaoSla decisao = RegraAlertaSla.avaliar(
                leito.getStatus(),
                leito.getDataUltimaAtualizacaoStatus(),
                sla.prazoAlertaMin(),
                sla.prazoAcaoMin(),
                sla.acaoAutomatica(),
                agora);

        if (decisao == DecisaoSla.NADA) {
            return;
        }

        int minutos = RegraAlertaSla.minutosSemAtualizacao(leito.getDataUltimaAtualizacaoStatus(), agora);
        AlertaLeito alerta = alertaService.garantirAberto(leito, leito.getStatus(), minutos, agora);

        if (decisao == DecisaoSla.ABRIR_ALERTA_E_LIBERAR) {
            leitoService.transicionar(
                    leito.getId(),
                    EventoLeito.FINALIZAR_HIGIENIZACAO,
                    Autor.SISTEMA,
                    MOTIVO_TIMEOUT_HIGIENIZACAO);
            leitoSlaRepository.marcarLiberadoAutomaticamente(leito.getId());
            alertaService.registrarAcaoExecutada(alerta.getId(), AcaoAutomatica.LIBERAR_LEITO);
        }
    }
}
