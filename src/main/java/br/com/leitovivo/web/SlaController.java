package br.com.leitovivo.web;

import br.com.leitovivo.service.SlaService;
import br.com.leitovivo.web.dto.AtualizarSlaRequest;
import br.com.leitovivo.web.dto.SlaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "SLA")
@RequestMapping("/sla")
public class SlaController {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping
    @Operation(summary = "Listar SLAs")
    public List<SlaResponse> listar() {
        return slaService.listar();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar prazos de SLA", description = "Efeito na próxima execução do job, sem reiniciar.")
    public SlaResponse atualizar(@PathVariable UUID id, @RequestBody AtualizarSlaRequest request) {
        return slaService.atualizar(id, request);
    }
}
