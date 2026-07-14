package br.com.leitovivo.service;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.sla.SituacaoAlerta;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.AlertaIndicadorRepository;
import br.com.leitovivo.persistence.HistoricoIndicadorRepository;
import br.com.leitovivo.persistence.HistoricoStatusLeito;
import br.com.leitovivo.persistence.Internacao;
import br.com.leitovivo.persistence.InternacaoIndicadorRepository;
import br.com.leitovivo.persistence.LeitoBuscaIndicadorRepository;
import br.com.leitovivo.persistence.StatusInternacao;
import br.com.leitovivo.persistence.UnidadeRepository;
import br.com.leitovivo.web.dto.ContagemPorStatusResponse;
import br.com.leitovivo.web.dto.IndicadoresUnidadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IndicadorService {

    private final UnidadeRepository unidadeRepository;
    private final LeitoBuscaIndicadorRepository leitoBuscaIndicadorRepository;
    private final InternacaoIndicadorRepository internacaoIndicadorRepository;
    private final HistoricoIndicadorRepository historicoIndicadorRepository;
    private final AlertaIndicadorRepository alertaIndicadorRepository;

    public IndicadorService(
            UnidadeRepository unidadeRepository,
            LeitoBuscaIndicadorRepository leitoBuscaIndicadorRepository,
            InternacaoIndicadorRepository internacaoIndicadorRepository,
            HistoricoIndicadorRepository historicoIndicadorRepository,
            AlertaIndicadorRepository alertaIndicadorRepository) {
        this.unidadeRepository = unidadeRepository;
        this.leitoBuscaIndicadorRepository = leitoBuscaIndicadorRepository;
        this.internacaoIndicadorRepository = internacaoIndicadorRepository;
        this.historicoIndicadorRepository = historicoIndicadorRepository;
        this.alertaIndicadorRepository = alertaIndicadorRepository;
    }

    @Transactional(readOnly = true)
    public IndicadoresUnidadeResponse calcular(UUID unidadeId) {
        if (!unidadeRepository.existsById(unidadeId)) {
            throw new RecursoNaoEncontradoException("Unidade não encontrada: " + unidadeId);
        }

        long total = leitoBuscaIndicadorRepository.countByUnidadeId(unidadeId);
        long ocupados = leitoBuscaIndicadorRepository.countByUnidadeIdAndStatus(unidadeId, StatusLeito.OCUPADO);
        double taxa = total == 0 ? 0.0 : (ocupados * 100.0) / total;

        ContagemPorStatusResponse contagem = contagemPorStatus(unidadeId);
        Double permanencia = permanenciaMediaMinutos(unidadeId);
        Double giro = giroMedioMinutos(unidadeId);
        long alertas = alertaIndicadorRepository.countByUnidadeIdAndSituacao(unidadeId, SituacaoAlerta.ABERTO);
        long liberados = leitoBuscaIndicadorRepository.countByUnidadeIdAndLiberadoAutomaticamenteTrue(unidadeId);

        return new IndicadoresUnidadeResponse(
                unidadeId, taxa, contagem, permanencia, giro, alertas, liberados);
    }

    private ContagemPorStatusResponse contagemPorStatus(UUID unidadeId) {
        Map<StatusLeito, Long> mapa = new EnumMap<>(StatusLeito.class);
        for (StatusLeito s : StatusLeito.values()) {
            mapa.put(s, 0L);
        }
        for (Object[] row : leitoBuscaIndicadorRepository.countGroupedByStatus(unidadeId)) {
            mapa.put((StatusLeito) row[0], (Long) row[1]);
        }
        return new ContagemPorStatusResponse(
                mapa.get(StatusLeito.LIVRE),
                mapa.get(StatusLeito.RESERVADO),
                mapa.get(StatusLeito.OCUPADO),
                mapa.get(StatusLeito.EM_HIGIENIZACAO),
                mapa.get(StatusLeito.MANUTENCAO));
    }

    private Double permanenciaMediaMinutos(UUID unidadeId) {
        List<Internacao> encerradas = internacaoIndicadorRepository.findByUnidadeIdAndStatus(
                unidadeId, StatusInternacao.ENCERRADA);
        if (encerradas.isEmpty()) {
            return null;
        }
        double soma = 0;
        int n = 0;
        for (Internacao i : encerradas) {
            if (i.getDataAlta() == null) {
                continue;
            }
            soma += Duration.between(i.getDataEntrada(), i.getDataAlta()).toMinutes();
            n++;
        }
        return n == 0 ? null : soma / n;
    }

    private Double giroMedioMinutos(UUID unidadeId) {
        List<HistoricoStatusLeito> historicos =
                historicoIndicadorRepository.findByUnidadeIdOrderByLeitoAndData(unidadeId);
        if (historicos.isEmpty()) {
            return null;
        }

        List<Long> intervalos = new ArrayList<>();
        UUID leitoAtual = null;
        Instant inicioHigienizacao = null;

        for (HistoricoStatusLeito h : historicos) {
            UUID leitoId = h.getLeito().getId();
            if (leitoAtual == null || !leitoAtual.equals(leitoId)) {
                leitoAtual = leitoId;
                inicioHigienizacao = null;
            }
            if (h.getStatusNovo() == StatusLeito.EM_HIGIENIZACAO) {
                inicioHigienizacao = h.getDataHora();
            } else if (h.getStatusNovo() == StatusLeito.LIVRE && inicioHigienizacao != null) {
                intervalos.add(Duration.between(inicioHigienizacao, h.getDataHora()).toMinutes());
                inicioHigienizacao = null;
            }
        }

        if (intervalos.isEmpty()) {
            return null;
        }
        return intervalos.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}
