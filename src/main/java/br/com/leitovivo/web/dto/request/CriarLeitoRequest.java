package br.com.leitovivo.web.dto.request;

import br.com.leitovivo.domain.leito.enums.TipoLeito;

import java.util.UUID;

public record CriarLeitoRequest(UUID unidadeId, String codigo, TipoLeito tipo) {
}
