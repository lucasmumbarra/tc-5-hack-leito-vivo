package br.com.leitovivo.web.mapper;

import br.com.leitovivo.persistence.entity.Paciente;
import br.com.leitovivo.web.dto.response.PacienteResponse;

public final class PacienteMapper {

  private PacienteMapper() {
  }

  public static PacienteResponse toResponse(Paciente paciente) {
    return new PacienteResponse(
        paciente.getId(),
        paciente.getNome(),
        paciente.getDataNascimento(),
        paciente.getCartaoSus());
  }
}
