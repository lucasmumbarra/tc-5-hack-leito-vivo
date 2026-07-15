package br.com.leitovivo.web.dto.response;

import java.util.UUID;

public record IndicadoresUnidadeResponse(
    UUID unidadeId,
    double taxaOcupacaoPercentual,
    ContagemPorStatusResponse contagemPorStatus,
    Double permanenciaMediaMinutos,
    Double giroMedioMinutos,
    long alertasAbertos,
    long leitosLiberadosAutomaticamente) {
}
