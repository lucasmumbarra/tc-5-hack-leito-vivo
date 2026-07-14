package br.com.leitovivo.web.mapper;

import br.com.leitovivo.persistence.entity.HistoricoStatusLeito;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.web.dto.response.HistoricoStatusResponse;
import br.com.leitovivo.web.dto.response.LeitoResponse;

public final class LeitoMapper {

    private LeitoMapper() {
    }

    public static LeitoResponse toResponse(Leito leito) {
        return new LeitoResponse(
                leito.getId(),
                leito.getUnidade().getId(),
                leito.getCodigo(),
                leito.getTipo(),
                leito.getStatus(),
                leito.getVersao(),
                leito.isLiberadoAutomaticamente(),
                leito.getDataUltimaAtualizacaoStatus());
    }

    public static HistoricoStatusResponse toHistoricoResponse(HistoricoStatusLeito historico) {
        return new HistoricoStatusResponse(
                historico.getId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getEvento(),
                historico.getAutor(),
                historico.getMotivo(),
                historico.getDataHora());
    }
}
