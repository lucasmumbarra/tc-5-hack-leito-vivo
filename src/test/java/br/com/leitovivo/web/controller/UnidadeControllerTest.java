package br.com.leitovivo.web.controller;

import br.com.leitovivo.web.handler.GlobalExceptionHandler;

import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.service.UnidadeService;
import br.com.leitovivo.web.dto.request.CriarUnidadeRequest;
import br.com.leitovivo.web.dto.response.UnidadeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UnidadeControllerTest {

    @Mock
    private UnidadeService unidadeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UnidadeController(unidadeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void criarRetorna201ComId() throws Exception {
        UUID id = UUID.randomUUID();
        when(unidadeService.criar(any(CriarUnidadeRequest.class)))
                .thenReturn(new UnidadeResponse(id, "Hospital", "São Paulo", "Sudeste", "Geral"));

        mockMvc.perform(post("/unidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Hospital","municipio":"São Paulo","regiao":"Sudeste","tipo":"Geral"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        UUID id = UUID.randomUUID();
        when(unidadeService.buscarPorId(id)).thenThrow(new RecursoNaoEncontradoException("Unidade não encontrada"));

        mockMvc.perform(get("/unidades/" + id))
                .andExpect(status().isNotFound());
    }
}
