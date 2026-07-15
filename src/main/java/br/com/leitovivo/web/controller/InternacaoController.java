package br.com.leitovivo.web.controller;

import br.com.leitovivo.service.InternacaoService;
import br.com.leitovivo.web.doc.InternacaoControllerDoc;
import br.com.leitovivo.web.dto.request.CriarInternacaoRequest;
import br.com.leitovivo.web.dto.response.InternacaoResponse;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController implements InternacaoControllerDoc {

  private final InternacaoService internacaoService;

  public InternacaoController(InternacaoService internacaoService) {
    this.internacaoService = internacaoService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Override
  public InternacaoResponse internar(

      @RequestBody CriarInternacaoRequest request) {
    return internacaoService.internar(request);
  }

  @PatchMapping("/{id}/alta")
  @Override
  public InternacaoResponse alta(
      @PathVariable UUID id,
      @RequestParam(required = false) String motivo) {
    return internacaoService.registrarAlta(id, motivo);
  }
}
