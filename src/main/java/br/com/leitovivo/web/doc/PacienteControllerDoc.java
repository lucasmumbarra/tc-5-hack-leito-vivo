package br.com.leitovivo.web.doc;

import br.com.leitovivo.web.dto.request.CriarPacienteRequest;
import br.com.leitovivo.web.dto.response.PacienteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import br.com.leitovivo.web.handler.ErroResponse;



@Tag(name = "Pacientes", description = "Cadastro de pacientes")
public interface PacienteControllerDoc {

    
    @Operation(summary = "Criar paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente criado",
                    content = @Content(schema = @Schema(implementation = PacienteResponse.class),
                            examples = @ExampleObject(value = """
                                    {"id":"3fa85f64-5717-4562-b3fc-2c963f66afa6","nome":"Maria Silva","dataNascimento":"1980-05-12","cartaoSus":"123456789012345"}
                                    """))),
            @ApiResponse(responseCode = "422", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public PacienteResponse criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CriarPacienteRequest.class),
                            examples = @ExampleObject(value = """
                                    {"nome":"Maria Silva","dataNascimento":"1980-05-12","cartaoSus":"123456789012345"}
                                    """)))
             CriarPacienteRequest request);
}
