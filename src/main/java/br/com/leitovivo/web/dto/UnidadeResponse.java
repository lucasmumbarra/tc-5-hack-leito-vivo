package br.com.leitovivo.web.dto;

import java.util.UUID;

public record UnidadeResponse(UUID id, String nome, String municipio, String regiao, String tipo) {
}
