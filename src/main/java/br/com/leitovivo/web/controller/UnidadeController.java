package br.com.leitovivo.web.controller;

import br.com.leitovivo.service.UnidadeService;
import br.com.leitovivo.web.doc.UnidadeControllerDoc;
import br.com.leitovivo.web.dto.request.CriarUnidadeRequest;
import br.com.leitovivo.web.dto.response.UnidadeResponse;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/unidades")
public class UnidadeController implements UnidadeControllerDoc {

  private final UnidadeService unidadeService;

  public UnidadeController(UnidadeService unidadeService) {
    this.unidadeService = unidadeService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Override
  public UnidadeResponse criar(

      @RequestBody CriarUnidadeRequest request) {
    return unidadeService.criar(request);
  }

  @GetMapping("/{id}")
  @Override
  public UnidadeResponse buscar(@PathVariable UUID id) {
    return unidadeService.buscarPorId(id);
  }
}
