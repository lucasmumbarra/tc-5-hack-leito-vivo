package br.com.leitovivo.web.mapper;

import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.web.dto.response.LeitoCompativelResponse;

public final class BuscaLeitoMapper {

  private BuscaLeitoMapper() {
  }

  public static LeitoCompativelResponse toResponse(Leito leito) {
    return new LeitoCompativelResponse(
        leito.getId(),
        leito.getUnidade().getId(),
        leito.getUnidade().getNome(),
        leito.getUnidade().getRegiao(),
        leito.getCodigo(),
        leito.getTipo());
  }
}
