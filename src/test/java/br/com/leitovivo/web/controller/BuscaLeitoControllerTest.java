package br.com.leitovivo.web.controller;

import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.service.BuscaLeitoService;
import br.com.leitovivo.web.dto.response.LeitoCompativelResponse;
import br.com.leitovivo.web.handler.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BuscaLeitoControllerTest {

  @Mock
  private BuscaLeitoService buscaLeitoService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new BuscaLeitoController(buscaLeitoService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void buscaRetornaLista() throws Exception {
    UUID id = UUID.randomUUID();
    UUID unidadeId = UUID.randomUUID();
    when(buscaLeitoService.buscarCompativeis(TipoLeito.UTI, "Sudeste"))
        .thenReturn(List.of(new LeitoCompativelResponse(
            id, unidadeId, "Hospital A", "Sudeste", "UTI-01", TipoLeito.UTI)));

    mockMvc.perform(get("/leitos/compativeis")
            .param("necessidade", "UTI")
            .param("regiao", "Sudeste"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].codigo").value("UTI-01"))
        .andExpect(jsonPath("$[0].unidadeNome").value("Hospital A"));
  }

  @Test
  void buscaVaziaRetorna200() throws Exception {
    when(buscaLeitoService.buscarCompativeis(eq(TipoLeito.UTI), eq("Norte"))).thenReturn(List.of());

    mockMvc.perform(get("/leitos/compativeis")
            .param("necessidade", "UTI")
            .param("regiao", "Norte"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void necessidadeInvalida422() throws Exception {
    when(buscaLeitoService.buscarCompativeis(eq(TipoLeito.UTI), eq("")))
        .thenThrow(new PayloadInvalidoException("regiao é obrigatória"));

    // Missing regiao → Spring may bind empty; also test service 422 path via blank
    mockMvc.perform(get("/leitos/compativeis")
            .param("necessidade", "UTI")
            .param("regiao", ""))
        .andExpect(status().isUnprocessableEntity());
  }
}
