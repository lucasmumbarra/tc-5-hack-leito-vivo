package br.com.leitovivo.web.dto;

import java.util.UUID;

public record CriarInternacaoRequest(UUID leitoId, UUID pacienteId, String motivo) {
}
