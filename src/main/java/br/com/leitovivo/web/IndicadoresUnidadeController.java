package br.com.leitovivo.web;

import br.com.leitovivo.service.IndicadorService;
import br.com.leitovivo.web.dto.IndicadoresUnidadeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/unidades")
@Tag(name = "Indicadores")
public class IndicadoresUnidadeController {

    private final IndicadorService indicadorService;

    public IndicadoresUnidadeController(IndicadorService indicadorService) {
        this.indicadorService = indicadorService;
    }

    @GetMapping("/{id}/indicadores")
    @Operation(
            summary = "Indicadores da unidade",
            description = "Taxa de ocupação, contagem por status, permanência média, giro EM_HIGIENIZACAO→LIVRE, "
                    + "alertas abertos e liberações automáticas. Sem IA.")
    public IndicadoresUnidadeResponse indicadores(@PathVariable UUID id) {
        return indicadorService.calcular(id);
    }
}
