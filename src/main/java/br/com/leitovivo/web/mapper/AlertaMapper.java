package br.com.leitovivo.web.mapper;

import br.com.leitovivo.persistence.entity.AlertaLeito;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.web.dto.response.AlertaResponse;

public final class AlertaMapper {

    private AlertaMapper() {
    }

    public static AlertaResponse toResponse(AlertaLeito alerta) {
        Leito leito = alerta.getLeito();
        return new AlertaResponse(
                alerta.getId(),
                leito.getId(),
                leito.getUnidade().getId(),
                leito.getTipo(),
                alerta.getStatusEmAlerta(),
                alerta.getSituacao(),
                alerta.getMinutosSemAtualizacao(),
                alerta.getAcaoExecutada(),
                alerta.getDataAbertura(),
                alerta.getDataResolucao(),
                alerta.getResolvidoPor());
    }
}
