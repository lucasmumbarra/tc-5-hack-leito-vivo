package br.com.leitovivo.web.controller;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.web.doc.LeitoControllerDoc;
import br.com.leitovivo.service.LeitoService;
import br.com.leitovivo.web.dto.request.CriarLeitoRequest;
import br.com.leitovivo.web.dto.response.HistoricoStatusResponse;
import br.com.leitovivo.web.dto.response.LeitoResponse;
import br.com.leitovivo.web.dto.request.TransicionarLeitoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leitos")

public class LeitoController implements LeitoControllerDoc {

    private final LeitoService leitoService;

    public LeitoController(LeitoService leitoService) {
        this.leitoService = leitoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    
    
    @Override
    public LeitoResponse criar(
            
            @RequestBody CriarLeitoRequest request) {
        return leitoService.criar(request);
    }

    @GetMapping
    
    
    @Override
    public List<LeitoResponse> listar(
            @RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) TipoLeito tipo,
            @RequestParam(required = false) StatusLeito status) {
        return leitoService.listar(unidadeId, tipo, status);
    }

    @PatchMapping("/{id}/status")
    
    
    @Override
    public LeitoResponse transicionar(
            @PathVariable UUID id,
            
            @RequestBody TransicionarLeitoRequest request) {
        return leitoService.transicionar(id, request);
    }

    @GetMapping("/{id}/historico")
    
    
    @Override
    public List<HistoricoStatusResponse> historico(@PathVariable UUID id) {
        return leitoService.listarHistorico(id);
    }
}
