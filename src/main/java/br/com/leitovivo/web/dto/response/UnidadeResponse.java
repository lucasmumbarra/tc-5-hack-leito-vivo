package br.com.leitovivo.web.dto.response;

import java.util.UUID;

public record UnidadeResponse(UUID id, String nome, String municipio, String regiao, String tipo) {
}
