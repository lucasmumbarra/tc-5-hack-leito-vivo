package br.com.leitovivo.web.controller;

import br.com.leitovivo.service.PacienteService;
import br.com.leitovivo.web.doc.PacienteControllerDoc;
import br.com.leitovivo.web.dto.request.CriarPacienteRequest;
import br.com.leitovivo.web.dto.response.PacienteResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
public class PacienteController implements PacienteControllerDoc {

  private final PacienteService pacienteService;

  public PacienteController(PacienteService pacienteService) {
    this.pacienteService = pacienteService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Override
  public PacienteResponse criar(

      @RequestBody CriarPacienteRequest request) {
    return pacienteService.criar(request);
  }
}
