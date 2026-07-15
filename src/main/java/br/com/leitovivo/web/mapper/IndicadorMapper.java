package br.com.leitovivo.web.mapper;

import br.com.leitovivo.web.dto.response.ContagemPorStatusResponse;
import br.com.leitovivo.web.dto.response.IndicadoresUnidadeResponse;

import java.util.UUID;

public final class IndicadorMapper {

  private IndicadorMapper() {
  }

  public static IndicadoresUnidadeResponse toResponse(
      UUID unidadeId,
      double taxaOcupacaoPercent,
      ContagemPorStatusResponse contagemPorStatus,
      Double permanenciaMediaMinutos,
      Double giroMedioMinutos,
      long alertasAbertos,
      long liberadosAutomaticos) {
    return new IndicadoresUnidadeResponse(
        unidadeId,
        taxaOcupacaoPercent,
        contagemPorStatus,
        permanenciaMediaMinutos,
        giroMedioMinutos,
        alertasAbertos,
        liberadosAutomaticos);
  }

  public static ContagemPorStatusResponse toContagemPorStatus(
      long livre, long reservado, long ocupado, long emHigienizacao, long emManutencao) {
    return new ContagemPorStatusResponse(livre, reservado, ocupado, emHigienizacao, emManutencao);
  }
}
