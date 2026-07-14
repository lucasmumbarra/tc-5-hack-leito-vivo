package br.com.leitovivo.web.controller;

import br.com.leitovivo.web.handler.GlobalExceptionHandler;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.service.AlertaService;
import br.com.leitovivo.web.dto.response.AlertaResponse;
import br.com.leitovivo.web.dto.request.ResolverAlertaRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertaControllerTest {

    @Mock
    private AlertaService alertaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AlertaController(alertaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarFiltraPorUnidadeESituacao() throws Exception {
        UUID unidadeId = UUID.randomUUID();
        UUID alertaId = UUID.randomUUID();
        when(alertaService.listar(eq(unidadeId), isNull(), isNull(), eq(SituacaoAlerta.ABERTO)))
                .thenReturn(List.of(new AlertaResponse(
                        alertaId, UUID.randomUUID(), unidadeId, TipoLeito.UTI,
                        StatusLeito.OCUPADO, SituacaoAlerta.ABERTO, 40000, null,
                        Instant.parse("2026-07-13T10:00:00Z"), null, null)));

        mockMvc.perform(get("/leitos/alertas")
                        .param("unidadeId", unidadeId.toString())
                        .param("situacao", "ABERTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(alertaId.toString()))
                .andExpect(jsonPath("$[0].situacao").value("ABERTO"));
    }

    @Test
    void resolverJaResolvidoRetorna409() throws Exception {
        UUID id = UUID.randomUUID();
        when(alertaService.resolver(eq(id), any(ResolverAlertaRequest.class)))
                .thenThrow(new ConflitoNegocioException("Alerta já está resolvido"));

        mockMvc.perform(patch("/alertas/{id}/resolver", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolvidoPor\":\"ana\"}"))
                .andExpect(status().isConflict());
    }
}
