package br.com.leitovivo.web.doc;

import br.com.leitovivo.web.dto.response.IndicadoresUnidadeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;



@Tag(name = "Indicadores")
public interface IndicadoresUnidadeControllerDoc {

    
    @Operation(
            summary = "Indicadores da unidade",
            description = "Taxa de ocupação, contagem por status, permanência média, giro EM_HIGIENIZACAO→LIVRE, "
                    + "alertas abertos e liberações automáticas. Sem IA.")
    public IndicadoresUnidadeResponse indicadores( UUID id);
}
