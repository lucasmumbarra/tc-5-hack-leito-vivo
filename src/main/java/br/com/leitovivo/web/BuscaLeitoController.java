package br.com.leitovivo.web;

import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.service.BuscaLeitoService;
import br.com.leitovivo.web.dto.LeitoCompativelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leitos")
@Tag(name = "Busca de leitos")
public class BuscaLeitoController {

    private final BuscaLeitoService buscaLeitoService;

    public BuscaLeitoController(BuscaLeitoService buscaLeitoService) {
        this.buscaLeitoService = buscaLeitoService;
    }

    @GetMapping("/compativeis")
    @Operation(
            summary = "Buscar leitos compatíveis",
            description = "Retorna leitos LIVRES cujo tipo atende a necessidade (MatrizCompatibilidadeLeito) na região. "
                    + "Reserva: use PATCH /leitos/{id}/status com RESERVAR_LEITO (funil existente).")
    public List<LeitoCompativelResponse> buscarCompativeis(
            @RequestParam TipoLeito necessidade,
            @RequestParam String regiao) {
        return buscaLeitoService.buscarCompativeis(necessidade, regiao);
    }
}
