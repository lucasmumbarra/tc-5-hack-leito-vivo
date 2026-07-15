package br.com.leitovivo.web.doc;

import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.web.dto.response.LeitoCompativelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;


@Tag(name = "Busca de leitos")
public interface BuscaLeitoControllerDoc {

  @Operation(
      summary = "Buscar leitos compatíveis",
      description = "Retorna leitos LIVRES cujo tipo atende a necessidade (MatrizCompatibilidadeLeito) na região. "
          + "Reserva: use PATCH /leitos/{id}/status com RESERVAR_LEITO (funil existente).")
  public List<LeitoCompativelResponse> buscarCompativeis(
      TipoLeito necessidade,
      String regiao);
}
