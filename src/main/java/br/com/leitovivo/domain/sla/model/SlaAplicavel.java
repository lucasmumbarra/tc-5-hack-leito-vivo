package br.com.leitovivo.domain.sla.model;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;

import java.util.UUID;

public record SlaAplicavel(
    UUID id,
    UUID unidadeId,
    TipoLeito tipoLeito,
    StatusLeito status,
    int prazoAlertaMin,
    Integer prazoAcaoMin,
    AcaoAutomatica acaoAutomatica) {
}
