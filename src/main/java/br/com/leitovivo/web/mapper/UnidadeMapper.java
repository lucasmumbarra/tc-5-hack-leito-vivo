package br.com.leitovivo.web.mapper;

import br.com.leitovivo.persistence.entity.Unidade;
import br.com.leitovivo.web.dto.response.UnidadeResponse;

public final class UnidadeMapper {

    private UnidadeMapper() {
    }

    public static UnidadeResponse toResponse(Unidade unidade) {
        return new UnidadeResponse(
                unidade.getId(),
                unidade.getNome(),
                unidade.getMunicipio(),
                unidade.getRegiao(),
                unidade.getTipo());
    }
}
