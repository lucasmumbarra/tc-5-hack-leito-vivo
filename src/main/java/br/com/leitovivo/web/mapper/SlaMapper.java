package br.com.leitovivo.web.mapper;

import br.com.leitovivo.domain.sla.model.SlaAplicavel;
import br.com.leitovivo.persistence.entity.SlaStatusLeito;
import br.com.leitovivo.web.dto.response.SlaResponse;

public final class SlaMapper {

  private SlaMapper() {
  }

  public static SlaResponse toResponse(SlaStatusLeito sla) {
    return new SlaResponse(
        sla.getId(),
        sla.getUnidadeId(),
        sla.getTipoLeito(),
        sla.getStatus(),
        sla.getPrazoAlertaMin(),
        sla.getPrazoAcaoMin(),
        sla.getAcaoAutomatica());
  }

  public static SlaAplicavel toRegra(SlaStatusLeito sla) {
    return new SlaAplicavel(
        sla.getId(),
        sla.getUnidadeId(),
        sla.getTipoLeito(),
        sla.getStatus(),
        sla.getPrazoAlertaMin(),
        sla.getPrazoAcaoMin(),
        sla.getAcaoAutomatica());
  }
}
