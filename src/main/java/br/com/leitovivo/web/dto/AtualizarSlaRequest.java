package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.sla.AcaoAutomaticaSla;

public record AtualizarSlaRequest(
        int prazoAlertaMin,
        Integer prazoAcaoMin,
        AcaoAutomaticaSla acaoAutomatica) {
}
