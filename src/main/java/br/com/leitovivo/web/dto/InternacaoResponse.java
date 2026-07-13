package br.com.leitovivo.web.dto;

import br.com.leitovivo.persistence.StatusInternacao;

import java.time.Instant;
import java.util.UUID;

public record InternacaoResponse(
        UUID id,
        UUID leitoId,
        UUID pacienteId,
        StatusInternacao status,
        Instant dataEntrada,
        Instant dataAlta) {
}
