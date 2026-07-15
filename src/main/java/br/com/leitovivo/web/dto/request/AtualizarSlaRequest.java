package br.com.leitovivo.web.dto.request;

import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;

public record AtualizarSlaRequest(
    int prazoAlertaMin,
    Integer prazoAcaoMin,
    AcaoAutomatica acaoAutomatica) {
}
