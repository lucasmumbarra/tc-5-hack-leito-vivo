package br.com.leitovivo.web.mapper;

import br.com.leitovivo.persistence.entity.Internacao;
import br.com.leitovivo.web.dto.response.InternacaoResponse;

public final class InternacaoMapper {

  private InternacaoMapper() {
  }

  public static InternacaoResponse toResponse(Internacao internacao) {
    return new InternacaoResponse(
        internacao.getId(),
        internacao.getLeito().getId(),
        internacao.getPaciente().getId(),
        internacao.getStatus(),
        internacao.getDataEntrada(),
        internacao.getDataAlta());
  }
}
