package br.com.leitovivo.web.doc;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;
import br.com.leitovivo.web.dto.request.ResolverAlertaRequest;
import br.com.leitovivo.web.dto.response.AlertaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;


@Tag(name = "Alertas")
public interface AlertaControllerDoc {

  @Operation(summary = "Listar alertas", description = "Filtros por unidade, tipo, status em alerta e situação.")
  public List<AlertaResponse> listar(
      UUID unidadeId,
      TipoLeito tipo,
      StatusLeito statusEmAlerta,
      SituacaoAlerta situacao);


  @Operation(summary = "Resolver alerta", description = "Somente ação humana (RN15). Já resolvido → 409.")
  public AlertaResponse resolver(UUID id, ResolverAlertaRequest request);
}
