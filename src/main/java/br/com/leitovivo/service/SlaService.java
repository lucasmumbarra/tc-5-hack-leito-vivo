package br.com.leitovivo.service;

import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.model.SlaAplicavel;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.entity.SlaStatusLeito;
import br.com.leitovivo.persistence.repository.SlaStatusLeitoRepository;
import br.com.leitovivo.web.dto.request.AtualizarSlaRequest;
import br.com.leitovivo.web.dto.response.SlaResponse;
import br.com.leitovivo.web.mapper.SlaMapper;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlaService {

  private final SlaStatusLeitoRepository slaStatusLeitoRepository;

  public SlaService(SlaStatusLeitoRepository slaStatusLeitoRepository) {
    this.slaStatusLeitoRepository = slaStatusLeitoRepository;
  }

  @Transactional(readOnly = true)
  public List<SlaResponse> listar() {
    return slaStatusLeitoRepository.findAll().stream()
        .map(SlaMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SlaAplicavel> listarComoRegras() {
    return slaStatusLeitoRepository.findAll().stream()
        .map(SlaMapper::toRegra)
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
    if (request.acaoAutomatica() == AcaoAutomatica.LIBERAR_LEITO) {
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
    return SlaMapper.toResponse(sla);
  }

}
