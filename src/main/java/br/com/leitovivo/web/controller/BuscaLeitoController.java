package br.com.leitovivo.web.controller;

import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.service.BuscaLeitoService;
import br.com.leitovivo.web.doc.BuscaLeitoControllerDoc;
import br.com.leitovivo.web.dto.response.LeitoCompativelResponse;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leitos")
public class BuscaLeitoController implements BuscaLeitoControllerDoc {

  private final BuscaLeitoService buscaLeitoService;

  public BuscaLeitoController(BuscaLeitoService buscaLeitoService) {
    this.buscaLeitoService = buscaLeitoService;
  }

  @GetMapping("/compativeis")
  @Override
  public List<LeitoCompativelResponse> buscarCompativeis(
      @RequestParam TipoLeito necessidade,
      @RequestParam String regiao) {
    return buscaLeitoService.buscarCompativeis(necessidade, regiao);
  }
}
