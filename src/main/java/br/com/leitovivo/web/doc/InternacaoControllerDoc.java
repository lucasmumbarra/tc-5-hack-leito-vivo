package br.com.leitovivo.web.doc;

import br.com.leitovivo.web.dto.request.CriarInternacaoRequest;
import br.com.leitovivo.web.dto.response.InternacaoResponse;
import br.com.leitovivo.web.handler.ErroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

import org.springframework.http.MediaType;


@Tag(name = "Internações", description = "Internação e alta — status do leito só via funil")
public interface InternacaoControllerDoc {

  @Operation(summary = "Internar paciente", description = "Só em leito LIVRE ou RESERVADO; move o leito via funil (INTERNAR_PACIENTE).")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Internação criada",
          content = @Content(schema = @Schema(implementation = InternacaoResponse.class))),
      @ApiResponse(responseCode = "404", description = "Leito ou paciente inexistente",
          content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "409", description = "Leito indisponível, paciente já internado ou conflito",
          content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "422", description = "Payload inválido",
          content = @Content(schema = @Schema(implementation = ErroResponse.class)))
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
      CriarInternacaoRequest request);


  @Operation(summary = "Registrar alta", description = "Encerra a internação e move o leito para EM_HIGIENIZACAO via funil.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Alta registrada",
          content = @Content(schema = @Schema(implementation = InternacaoResponse.class))),
      @ApiResponse(responseCode = "404", description = "Internação inexistente",
          content = @Content(schema = @Schema(implementation = ErroResponse.class))),
      @ApiResponse(responseCode = "409", description = "Já encerrada ou transição inválida",
          content = @Content(schema = @Schema(implementation = ErroResponse.class)))
  })
  public InternacaoResponse alta(
      UUID id,
      String motivo);
}
