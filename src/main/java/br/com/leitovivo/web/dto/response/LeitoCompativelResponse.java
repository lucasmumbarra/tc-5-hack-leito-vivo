package br.com.leitovivo.web.dto.response;

import br.com.leitovivo.domain.leito.enums.TipoLeito;

import java.util.UUID;

public record LeitoCompativelResponse(
    UUID id,
    UUID unidadeId,
    String unidadeNome,
    String unidadeRegiao,
    String codigo,
    TipoLeito tipo) {
}
