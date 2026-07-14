package br.com.leitovivo.web.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record PacienteResponse(UUID id, String nome, LocalDate dataNascimento, String cartaoSus) {
}
