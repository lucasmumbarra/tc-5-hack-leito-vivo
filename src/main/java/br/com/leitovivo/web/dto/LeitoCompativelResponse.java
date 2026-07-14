package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.TipoLeito;

import java.util.UUID;

public record LeitoCompativelResponse(
        UUID id,
        UUID unidadeId,
        String unidadeNome,
        String unidadeRegiao,
        String codigo,
        TipoLeito tipo) {
}
