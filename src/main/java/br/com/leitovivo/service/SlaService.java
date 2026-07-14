package br.com.leitovivo.service;

import br.com.leitovivo.domain.sla.AcaoAutomaticaSla;
import br.com.leitovivo.domain.sla.SlaRegra;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.SlaStatusLeito;
import br.com.leitovivo.persistence.SlaStatusLeitoRepository;
import br.com.leitovivo.web.dto.AtualizarSlaRequest;
import br.com.leitovivo.web.dto.SlaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SlaService {

    private final SlaStatusLeitoRepository slaStatusLeitoRepository;

    public SlaService(SlaStatusLeitoRepository slaStatusLeitoRepository) {
        this.slaStatusLeitoRepository = slaStatusLeitoRepository;
    }

    @Transactional(readOnly = true)
    public List<SlaResponse> listar() {
        return slaStatusLeitoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SlaRegra> listarComoRegras() {
        return slaStatusLeitoRepository.findAll().stream()
                .map(this::toRegra)
                .toList();
    }

    @Transactional
    public SlaResponse atualizar(UUID id, AtualizarSlaRequest request) {
        if (request == null) {
            throw new PayloadInvalidoException("Payload obrigatório");
        }
        if (request.prazoAlertaMin() <= 0) {
            throw new PayloadInvalidoException("prazoAlertaMin deve ser > 0");
        }
        if (request.acaoAutomatica() == null) {
            throw new PayloadInvalidoException("acaoAutomatica é obrigatória");
        }
        if (request.acaoAutomatica() == AcaoAutomaticaSla.LIBERAR_LEITO) {
            if (request.prazoAcaoMin() == null || request.prazoAcaoMin() <= 0) {
                throw new PayloadInvalidoException("prazoAcaoMin é obrigatório quando acaoAutomatica=LIBERAR_LEITO");
            }
            if (request.prazoAcaoMin() < request.prazoAlertaMin()) {
                throw new PayloadInvalidoException("prazoAcaoMin deve ser >= prazoAlertaMin");
            }
        }

        SlaStatusLeito sla = slaStatusLeitoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("SLA não encontrado: " + id));
        sla.atualizarPrazos(request.prazoAlertaMin(), request.prazoAcaoMin(), request.acaoAutomatica());
        return toResponse(sla);
    }

    private SlaResponse toResponse(SlaStatusLeito s) {
        return new SlaResponse(
                s.getId(),
                s.getUnidadeId(),
                s.getTipoLeito(),
                s.getStatus(),
                s.getPrazoAlertaMin(),
                s.getPrazoAcaoMin(),
                s.getAcaoAutomatica());
    }

    private SlaRegra toRegra(SlaStatusLeito s) {
        return new SlaRegra(
                s.getId(),
                s.getUnidadeId(),
                s.getTipoLeito(),
                s.getStatus(),
                s.getPrazoAlertaMin(),
                s.getPrazoAcaoMin(),
                s.getAcaoAutomatica());
    }
}
