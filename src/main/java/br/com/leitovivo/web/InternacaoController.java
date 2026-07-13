package br.com.leitovivo.web;

import br.com.leitovivo.service.InternacaoService;
import br.com.leitovivo.web.dto.CriarInternacaoRequest;
import br.com.leitovivo.web.dto.InternacaoResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internacoes")
@Tag(name = "Internações", description = "Internação e alta — status do leito só via funil")
public class InternacaoController {

    private final InternacaoService internacaoService;

    public InternacaoController(InternacaoService internacaoService) {
        this.internacaoService = internacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Internar paciente", description = "Só em leito LIVRE ou RESERVADO; move o leito via funil (INTERNAR_PACIENTE).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Internação criada",
                    content = @Content(schema = @Schema(implementation = InternacaoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leito ou paciente inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Leito indisponível, paciente já internado ou conflito",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public InternacaoResponse internar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CriarInternacaoRequest.class),
                            examples = @ExampleObject(value = """
                                    {"leitoId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","pacienteId":"3fa85f64-5717-4562-b3fc-2c963f66afa7","motivo":"Internação eletiva"}
                                    """)))
            @RequestBody CriarInternacaoRequest request) {
        return internacaoService.internar(request);
    }

    @PatchMapping("/{id}/alta")
    @Operation(summary = "Registrar alta", description = "Encerra a internação e move o leito para EM_HIGIENIZACAO via funil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alta registrada",
                    content = @Content(schema = @Schema(implementation = InternacaoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Internação inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Já encerrada ou transição inválida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public InternacaoResponse alta(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        return internacaoService.registrarAlta(id, motivo);
    }
}
