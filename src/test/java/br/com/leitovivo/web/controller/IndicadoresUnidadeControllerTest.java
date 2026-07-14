package br.com.leitovivo.web.controller;

import br.com.leitovivo.web.handler.GlobalExceptionHandler;

import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.service.IndicadorService;
import br.com.leitovivo.web.dto.response.ContagemPorStatusResponse;
import br.com.leitovivo.web.dto.response.IndicadoresUnidadeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IndicadoresUnidadeControllerTest {

    @Mock
    private IndicadorService indicadorService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new IndicadoresUnidadeController(indicadorService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void indicadoresRetorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(indicadorService.calcular(id)).thenReturn(new IndicadoresUnidadeResponse(
                id, 40.0, new ContagemPorStatusResponse(6, 0, 4, 0, 0),
                null, null, 1, 2));

        mockMvc.perform(get("/unidades/{id}/indicadores", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxaOcupacaoPercentual").value(40.0))
                .andExpect(jsonPath("$.contagemPorStatus.ocupado").value(4))
                .andExpect(jsonPath("$.alertasAbertos").value(1));
    }

    @Test
    void unidadeInexistente404() throws Exception {
        UUID id = UUID.randomUUID();
        when(indicadorService.calcular(id)).thenThrow(new RecursoNaoEncontradoException("não encontrada"));

        mockMvc.perform(get("/unidades/{id}/indicadores", id))
                .andExpect(status().isNotFound());
    }
}
