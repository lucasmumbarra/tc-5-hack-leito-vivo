package br.com.leitovivo.web.doc;

import br.com.leitovivo.web.dto.request.CriarUnidadeRequest;
import br.com.leitovivo.web.dto.response.UnidadeResponse;
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


@Tag(name = "Unidades", description = "Cadastro de unidades de saúde")
public interface UnidadeControllerDoc {

  @Operation(summary = "Criar unidade", description = "Cria uma unidade de saúde com nome, município, região e tipo.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Unidade criada",
          content = @Content(schema = @Schema(implementation = UnidadeResponse.class),
              examples = @ExampleObject(value = """
                  {"id":"3fa85f64-5717-4562-b3fc-2c963f66afa6","nome":"Hospital Municipal","municipio":"São Paulo","regiao":"Sudeste","tipo":"Hospital Geral"}
                  """))),
      @ApiResponse(responseCode = "422", description = "Payload inválido",
          content = @Content(schema = @Schema(implementation = ErroResponse.class)))
  })
  public UnidadeResponse criar(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CriarUnidadeRequest.class),
              examples = @ExampleObject(value = """
                  {"nome":"Hospital Municipal","municipio":"São Paulo","regiao":"Sudeste","tipo":"Hospital Geral"}
                  """)))
      CriarUnidadeRequest request);


  @Operation(summary = "Buscar unidade por id")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Unidade encontrada",
          content = @Content(schema = @Schema(implementation = UnidadeResponse.class))),
      @ApiResponse(responseCode = "404", description = "Unidade não encontrada",
          content = @Content(schema = @Schema(implementation = ErroResponse.class)))
  })
  public UnidadeResponse buscar(UUID id);
}
