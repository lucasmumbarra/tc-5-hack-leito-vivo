package br.com.leitovivo.web.doc;

import br.com.leitovivo.web.dto.request.AtualizarSlaRequest;
import br.com.leitovivo.web.dto.response.SlaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;


@Tag(name = "SLA")

public interface SlaControllerDoc {

    
    @Operation(summary = "Listar SLAs")
    public List<SlaResponse> listar();

    
    @Operation(summary = "Atualizar prazos de SLA", description = "Efeito na próxima execução do job, sem reiniciar.")
    public SlaResponse atualizar( UUID id,  AtualizarSlaRequest request);
}
