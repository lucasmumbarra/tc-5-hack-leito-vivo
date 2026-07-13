package br.com.leitovivo.web.dto;

import java.time.LocalDate;

public record CriarPacienteRequest(String nome, LocalDate dataNascimento, String cartaoSus) {
}
