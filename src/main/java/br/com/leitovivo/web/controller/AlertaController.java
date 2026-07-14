package br.com.leitovivo.web.controller;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;
import br.com.leitovivo.web.doc.AlertaControllerDoc;
import br.com.leitovivo.service.AlertaService;
import br.com.leitovivo.web.dto.response.AlertaResponse;
import br.com.leitovivo.web.dto.request.ResolverAlertaRequest;
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

@RequestMapping
public class AlertaController implements AlertaControllerDoc {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping("/leitos/alertas")
    
    @Override
    public List<AlertaResponse> listar(
            @RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) TipoLeito tipo,
            @RequestParam(required = false) StatusLeito statusEmAlerta,
            @RequestParam(required = false) SituacaoAlerta situacao) {
        return alertaService.listar(unidadeId, tipo, statusEmAlerta, situacao);
    }

    @PatchMapping("/alertas/{id}/resolver")
    @ResponseStatus(HttpStatus.OK)
    
    @Override
    public AlertaResponse resolver(@PathVariable UUID id, @RequestBody ResolverAlertaRequest request) {
        return alertaService.resolver(id, request);
    }
}
