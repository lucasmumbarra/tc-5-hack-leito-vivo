package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.domain.sla.AcaoAutomaticaSla;

import java.util.UUID;

public record SlaResponse(
        UUID id,
        UUID unidadeId,
        TipoLeito tipoLeito,
        StatusLeito status,
        int prazoAlertaMin,
        Integer prazoAcaoMin,
        AcaoAutomaticaSla acaoAutomatica) {
}
