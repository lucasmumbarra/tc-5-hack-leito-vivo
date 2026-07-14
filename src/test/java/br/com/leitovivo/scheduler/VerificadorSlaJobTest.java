package br.com.leitovivo.scheduler;

import br.com.leitovivo.domain.AutorAcao;
import br.com.leitovivo.domain.EventoLeito;
import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.domain.sla.AcaoAutomaticaSla;
import br.com.leitovivo.domain.sla.SituacaoAlerta;
import br.com.leitovivo.domain.sla.SlaRegra;
import br.com.leitovivo.persistence.AlertaLeito;
import br.com.leitovivo.persistence.Leito;
import br.com.leitovivo.persistence.LeitoSlaRepository;
import br.com.leitovivo.persistence.Unidade;
import br.com.leitovivo.service.AlertaService;
import br.com.leitovivo.service.LeitoService;
import br.com.leitovivo.service.SlaService;
import br.com.leitovivo.web.dto.LeitoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificadorSlaJobTest {

    private static final Instant AGORA = Instant.parse("2026-07-13T18:00:00Z");

    @Mock
    private SlaService slaService;
    @Mock
    private AlertaService alertaService;
    @Mock
    private LeitoService leitoService;
    @Mock
    private LeitoSlaRepository leitoSlaRepository;

    private VerificadorSlaJob job;
    private UUID unidadeId;
    private Leito leitoHigienizacao;
    private Leito leitoOcupado;
    private AlertaLeito alertaHigienizacao;
    private AlertaLeito alertaOcupado;

    @BeforeEach
    void setUp() throws Exception {
        job = new VerificadorSlaJob(
                slaService, alertaService, leitoService, leitoSlaRepository,
                Clock.fixed(AGORA, ZoneOffset.UTC));

        unidadeId = UUID.randomUUID();
        Unidade unidade = new Unidade("H", "SP", "SE", "Geral");
        setField(unidade, "id", unidadeId);

        leitoHigienizacao = new Leito(
                unidade, "HIG-01", TipoLeito.UTI, StatusLeito.EM_HIGIENIZACAO, false,
                AGORA.minus(5, ChronoUnit.HOURS));
        setField(leitoHigienizacao, "id", UUID.randomUUID());
        setField(leitoHigienizacao, "versao", 0L);

        leitoOcupado = new Leito(
                unidade, "OC-01", TipoLeito.CLINICO, StatusLeito.OCUPADO, false,
                AGORA.minus(25, ChronoUnit.DAYS));
        setField(leitoOcupado, "id", UUID.randomUUID());
        setField(leitoOcupado, "versao", 0L);

        alertaHigienizacao = new AlertaLeito(
                UUID.randomUUID(), leitoHigienizacao, StatusLeito.EM_HIGIENIZACAO,
                SituacaoAlerta.ABERTO, 300, AGORA);
        alertaOcupado = new AlertaLeito(
                UUID.randomUUID(), leitoOcupado, StatusLeito.OCUPADO,
                SituacaoAlerta.ABERTO, 36000, AGORA);
    }

    @Test
    void liberacaoAutomaticaPassaPeloFunilComSistema() {
        when(slaService.listarComoRegras()).thenReturn(List.of(
                new SlaRegra(UUID.randomUUID(), null, null, StatusLeito.EM_HIGIENIZACAO,
                        120, 240, AcaoAutomaticaSla.LIBERAR_LEITO)));
        when(leitoSlaRepository.findByStatusIn(any())).thenReturn(List.of(leitoHigienizacao));
        when(alertaService.garantirAberto(eq(leitoHigienizacao), eq(StatusLeito.EM_HIGIENIZACAO), anyInt(), eq(AGORA)))
                .thenReturn(alertaHigienizacao);
        when(leitoService.transicionar(
                leitoHigienizacao.getId(),
                EventoLeito.FINALIZAR_HIGIENIZACAO,
                AutorAcao.SISTEMA,
                VerificadorSlaJob.MOTIVO_TIMEOUT_HIGIENIZACAO))
                .thenReturn(new LeitoResponse(
                        leitoHigienizacao.getId(), unidadeId, "HIG-01", TipoLeito.UTI,
                        StatusLeito.LIVRE, 1L, true, AGORA));

        job.executar(AGORA);

        verify(leitoService).transicionar(
                leitoHigienizacao.getId(),
                EventoLeito.FINALIZAR_HIGIENIZACAO,
                AutorAcao.SISTEMA,
                VerificadorSlaJob.MOTIVO_TIMEOUT_HIGIENIZACAO);
        verify(leitoSlaRepository).marcarLiberadoAutomaticamente(leitoHigienizacao.getId());
        verify(alertaService).registrarAcaoExecutada(alertaHigienizacao.getId(), AcaoAutomaticaSla.LIBERAR_LEITO);
        verify(leitoSlaRepository, never()).save(any());
    }

    @Test
    void ocupadoGeraAlertaSemTransicionar() {
        when(slaService.listarComoRegras()).thenReturn(List.of(
                new SlaRegra(UUID.randomUUID(), null, null, StatusLeito.OCUPADO,
                        28800, null, AcaoAutomaticaSla.NENHUMA)));
        when(leitoSlaRepository.findByStatusIn(any())).thenReturn(List.of(leitoOcupado));
        when(alertaService.garantirAberto(eq(leitoOcupado), eq(StatusLeito.OCUPADO), anyInt(), eq(AGORA)))
                .thenReturn(alertaOcupado);

        job.executar(AGORA);

        verify(alertaService).garantirAberto(eq(leitoOcupado), eq(StatusLeito.OCUPADO), anyInt(), eq(AGORA));
        verify(leitoService, never()).transicionar(any(), any(), any(), any());
    }

    @Test
    void duasExecucoesNaoDuplicamAberturaViaGarantirAberto() {
        when(slaService.listarComoRegras()).thenReturn(List.of(
                new SlaRegra(UUID.randomUUID(), null, null, StatusLeito.OCUPADO,
                        28800, null, AcaoAutomaticaSla.NENHUMA)));
        when(leitoSlaRepository.findByStatusIn(any())).thenReturn(List.of(leitoOcupado));
        when(alertaService.garantirAberto(eq(leitoOcupado), eq(StatusLeito.OCUPADO), anyInt(), eq(AGORA)))
                .thenReturn(alertaOcupado);

        job.executar(AGORA);
        job.executar(AGORA);

        verify(alertaService, times(2)).garantirAberto(eq(leitoOcupado), eq(StatusLeito.OCUPADO), anyInt(), eq(AGORA));
        verify(leitoService, never()).transicionar(any(), any(), any(), any());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
