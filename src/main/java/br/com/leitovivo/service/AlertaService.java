package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.entity.AlertaLeito;
import br.com.leitovivo.persistence.repository.AlertaLeitoRepository;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.web.dto.response.AlertaResponse;
import br.com.leitovivo.web.dto.request.ResolverAlertaRequest;
import br.com.leitovivo.web.mapper.AlertaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AlertaService {

    private final AlertaLeitoRepository alertaLeitoRepository;
    private final Clock clock;

    public AlertaService(AlertaLeitoRepository alertaLeitoRepository, Clock clock) {
        this.alertaLeitoRepository = alertaLeitoRepository;
        this.clock = clock;
    }

    /**
     * Abre alerta ABERTO de forma idempotente (ON CONFLICT DO NOTHING).
     */
    @Transactional
    public AlertaLeito garantirAberto(Leito leito, StatusLeito statusEmAlerta, int minutos, Instant agora) {
        UUID candidatoId = UUID.randomUUID();
        int inseridos = alertaLeitoRepository.insertAbertoIgnoreConflict(
                candidatoId,
                leito.getId(),
                statusEmAlerta.name(),
                minutos,
                agora);

        AlertaLeito alerta = alertaLeitoRepository
                .findByLeitoIdAndStatusEmAlertaAndSituacao(leito.getId(), statusEmAlerta, SituacaoAlerta.ABERTO)
                .orElseThrow(() -> new IllegalStateException(
                        "Alerta ABERTO esperado após insert idempotente para leito " + leito.getId()));

        if (inseridos == 0) {
            alerta.atualizarMinutosSemAtualizacao(minutos);
        }
        return alerta;
    }

    @Transactional
    public void registrarAcaoExecutada(UUID alertaId, AcaoAutomatica acao) {
        AlertaLeito alerta = alertaLeitoRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado: " + alertaId));
        alerta.registrarAcaoExecutada(acao);
    }

    @Transactional
    public AlertaResponse resolver(UUID id, ResolverAlertaRequest request) {
        if (request == null || request.resolvidoPor() == null || request.resolvidoPor().isBlank()) {
            throw new PayloadInvalidoException("resolvidoPor é obrigatório");
        }
        AlertaLeito alerta = alertaLeitoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado: " + id));
        if (alerta.getSituacao() == SituacaoAlerta.RESOLVIDO) {
            throw new ConflitoNegocioException("Alerta já está resolvido: " + id);
        }
        alerta.resolver(request.resolvidoPor().trim(), Instant.now(clock));
        return AlertaMapper.toResponse(alerta);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar(
            UUID unidadeId, TipoLeito tipo, StatusLeito statusEmAlerta, SituacaoAlerta situacao) {
        return alertaLeitoRepository.filtrar(unidadeId, tipo, statusEmAlerta, situacao).stream()
                .map(AlertaMapper::toResponse)
                .toList();
    }

}
