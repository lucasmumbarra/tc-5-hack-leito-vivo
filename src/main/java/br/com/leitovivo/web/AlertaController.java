package br.com.leitovivo.web;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.domain.sla.SituacaoAlerta;
import br.com.leitovivo.service.AlertaService;
import br.com.leitovivo.web.dto.AlertaResponse;
import br.com.leitovivo.web.dto.ResolverAlertaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Alertas")
@RequestMapping
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping("/leitos/alertas")
    @Operation(summary = "Listar alertas", description = "Filtros por unidade, tipo, status em alerta e situação.")
    public List<AlertaResponse> listar(
            @RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) TipoLeito tipo,
            @RequestParam(required = false) StatusLeito statusEmAlerta,
            @RequestParam(required = false) SituacaoAlerta situacao) {
        return alertaService.listar(unidadeId, tipo, statusEmAlerta, situacao);
    }

    @PatchMapping("/alertas/{id}/resolver")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Resolver alerta", description = "Somente ação humana (RN15). Já resolvido → 409.")
    public AlertaResponse resolver(@PathVariable UUID id, @RequestBody ResolverAlertaRequest request) {
        return alertaService.resolver(id, request);
    }
}
