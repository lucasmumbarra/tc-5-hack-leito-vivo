package br.com.leitovivo.web;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.service.LeitoService;
import br.com.leitovivo.web.dto.CriarLeitoRequest;
import br.com.leitovivo.web.dto.HistoricoStatusResponse;
import br.com.leitovivo.web.dto.LeitoResponse;
import br.com.leitovivo.web.dto.TransicionarLeitoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Leitos", description = "Cadastro, consulta e transição de status via funil único")
public class LeitoController {

    private final LeitoService leitoService;

    public LeitoController(LeitoService leitoService) {
        this.leitoService = leitoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar leito",
            description = "Cria leito sempre com status LIVRE, liberado_automaticamente=false e data_ultima_atualizacao_status=agora (RN01). Status no payload é ignorado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leito criado",
                    content = @Content(schema = @Schema(implementation = LeitoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unidade inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Código duplicado na unidade",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public LeitoResponse criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CriarLeitoRequest.class),
                            examples = @ExampleObject(value = """
                                    {"unidadeId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","codigo":"UTI-01","tipo":"UTI"}
                                    """)))
            @RequestBody CriarLeitoRequest request) {
        return leitoService.criar(request);
    }

    @GetMapping
    @Operation(summary = "Listar leitos", description = "Filtros opcionais por unidadeId, tipo e status (AND).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de leitos (pode ser vazia)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeitoResponse.class))))
    })
    public List<LeitoResponse> listar(
            @RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) TipoLeito tipo,
            @RequestParam(required = false) StatusLeito status) {
        return leitoService.listar(unidadeId, tipo, status);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transicionar status", description = "Delega ao funil único LeitoService.transicionar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transição aplicada",
                    content = @Content(schema = @Schema(implementation = LeitoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leito inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Transição inválida ou conflito de versão",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public LeitoResponse transicionar(
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransicionarLeitoRequest.class),
                            examples = @ExampleObject(value = """
                                    {"evento":"RESERVAR_LEITO","autor":"USUARIO","motivo":"Transferência"}
                                    """)))
            @RequestBody TransicionarLeitoRequest request) {
        return leitoService.transicionar(id, request);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Histórico de status", description = "Trilha de auditoria em ordem cronológica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoricoStatusResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Leito inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<HistoricoStatusResponse> historico(@PathVariable UUID id) {
        return leitoService.listarHistorico(id);
    }
}
