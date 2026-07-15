package br.com.leitovivo.web.controller;

import br.com.leitovivo.service.IndicadorService;
import br.com.leitovivo.web.doc.IndicadoresUnidadeControllerDoc;
import br.com.leitovivo.web.dto.response.IndicadoresUnidadeResponse;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/unidades")
public class IndicadoresUnidadeController implements IndicadoresUnidadeControllerDoc {

  private final IndicadorService indicadorService;

  public IndicadoresUnidadeController(IndicadorService indicadorService) {
    this.indicadorService = indicadorService;
  }

  @GetMapping("/{id}/indicadores")
  @Override
  public IndicadoresUnidadeResponse indicadores(@PathVariable UUID id) {
    return indicadorService.calcular(id);
  }
}
