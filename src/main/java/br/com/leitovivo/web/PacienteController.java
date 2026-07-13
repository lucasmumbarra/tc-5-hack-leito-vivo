package br.com.leitovivo.web;

import br.com.leitovivo.service.PacienteService;
import br.com.leitovivo.web.dto.CriarPacienteRequest;
import br.com.leitovivo.web.dto.PacienteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
@Tag(name = "Pacientes", description = "Cadastro de pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente criado",
                    content = @Content(schema = @Schema(implementation = PacienteResponse.class),
                            examples = @ExampleObject(value = """
                                    {"id":"3fa85f64-5717-4562-b3fc-2c963f66afa6","nome":"Maria Silva","dataNascimento":"1980-05-12","cartaoSus":"123456789012345"}
                                    """))),
            @ApiResponse(responseCode = "422", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
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
            @RequestBody CriarPacienteRequest request) {
        return pacienteService.criar(request);
    }
}
