package br.com.leitovivo.web.dto.response;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;

import java.util.UUID;

public record SlaResponse(
    UUID id,
    UUID unidadeId,
    TipoLeito tipoLeito,
    StatusLeito status,
    int prazoAlertaMin,
    Integer prazoAcaoMin,
    AcaoAutomatica acaoAutomatica) {
}
