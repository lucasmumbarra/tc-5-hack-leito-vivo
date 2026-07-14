package br.com.leitovivo.domain.sla;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;

import java.util.UUID;

/**
 * Visão imutável de uma linha de SLA para resolução em cascata (sem JPA).
 */
public record SlaRegra(
        UUID id,
        UUID unidadeId,
        TipoLeito tipoLeito,
        StatusLeito status,
        int prazoAlertaMin,
        Integer prazoAcaoMin,
        AcaoAutomaticaSla acaoAutomatica) {
}
