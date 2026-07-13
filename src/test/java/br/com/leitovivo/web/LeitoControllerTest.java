package br.com.leitovivo.web;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.service.LeitoService;
import br.com.leitovivo.web.dto.CriarLeitoRequest;
import br.com.leitovivo.web.dto.LeitoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeitoControllerTest {

    @Mock
    private LeitoService leitoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LeitoController(leitoService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void criarRetorna201ComStatusLivre() throws Exception {
        UUID id = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        when(leitoService.criar(any(CriarLeitoRequest.class))).thenReturn(new LeitoResponse(
                id, unidadeId, "UTI-01", TipoLeito.UTI, StatusLeito.LIVRE, 0L, false, Instant.parse("2026-07-13T14:00:00Z")));

        mockMvc.perform(post("/leitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"unidadeId":"%s","codigo":"UTI-01","tipo":"UTI","status":"OCUPADO"}
                                """.formatted(unidadeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("LIVRE"))
                .andExpect(jsonPath("$.liberadoAutomaticamente").value(false));
    }

    @Test
    void unidadeInexistenteRetorna404() throws Exception {
        when(leitoService.criar(any())).thenThrow(new RecursoNaoEncontradoException("Unidade não encontrada"));

        mockMvc.perform(post("/leitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"unidadeId":"%s","codigo":"UTI-01","tipo":"UTI"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    void codigoDuplicadoRetorna409() throws Exception {
        when(leitoService.criar(any())).thenThrow(new ConflitoNegocioException("duplicado"));

        mockMvc.perform(post("/leitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"unidadeId":"%s","codigo":"UTI-01","tipo":"UTI"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isConflict());
    }

    @Test
    void filtroCombinadoRetornaLista() throws Exception {
        UUID unidadeId = UUID.randomUUID();
        when(leitoService.listar(eq(unidadeId), eq(TipoLeito.UTI), eq(StatusLeito.LIVRE)))
                .thenReturn(List.of(new LeitoResponse(
                        UUID.randomUUID(), unidadeId, "UTI-01", TipoLeito.UTI, StatusLeito.LIVRE,
                        0L, false, Instant.parse("2026-07-13T14:00:00Z"))));

        mockMvc.perform(get("/leitos")
                        .param("unidadeId", unidadeId.toString())
                        .param("tipo", "UTI")
                        .param("status", "LIVRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("UTI-01"));
    }

    @Test
    void listaVaziaRetorna200() throws Exception {
        when(leitoService.listar(isNull(), isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/leitos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
