package br.com.leitovivo.web.controller;

import br.com.leitovivo.web.doc.SlaControllerDoc;
import br.com.leitovivo.service.SlaService;
import br.com.leitovivo.web.dto.request.AtualizarSlaRequest;
import br.com.leitovivo.web.dto.response.SlaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController

@RequestMapping("/sla")
public class SlaController implements SlaControllerDoc {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping
    
    @Override
    public List<SlaResponse> listar() {
        return slaService.listar();
    }

    @PutMapping("/{id}")
    
    @Override
    public SlaResponse atualizar(@PathVariable UUID id, @RequestBody AtualizarSlaRequest request) {
        return slaService.atualizar(id, request);
    }
}
