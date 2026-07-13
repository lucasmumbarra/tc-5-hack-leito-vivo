package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.TipoLeito;

import java.util.UUID;

public record CriarLeitoRequest(UUID unidadeId, String codigo, TipoLeito tipo) {
}
