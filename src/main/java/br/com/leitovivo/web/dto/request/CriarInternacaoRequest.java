package br.com.leitovivo.web.dto.request;

import java.util.UUID;

public record CriarInternacaoRequest(UUID leitoId, UUID pacienteId, String motivo) {
}
