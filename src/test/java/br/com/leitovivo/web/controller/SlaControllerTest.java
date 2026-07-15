package br.com.leitovivo.web.controller;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.service.SlaService;
import br.com.leitovivo.web.dto.request.AtualizarSlaRequest;
import br.com.leitovivo.web.dto.response.SlaResponse;
import br.com.leitovivo.web.handler.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SlaControllerTest {

  @Mock
  private SlaService slaService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new SlaController(slaService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void listarRetornaSlas() throws Exception {
    UUID id = UUID.randomUUID();
    when(slaService.listar()).thenReturn(List.of(
        new SlaResponse(id, null, null, StatusLeito.OCUPADO, 28800, null, AcaoAutomatica.NENHUMA)));

    mockMvc.perform(get("/sla"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].prazoAlertaMin").value(28800));
  }

  @Test
  void atualizarAlteraPrazo() throws Exception {
    UUID id = UUID.randomUUID();
    when(slaService.atualizar(eq(id), any(AtualizarSlaRequest.class)))
        .thenReturn(new SlaResponse(id, null, null, StatusLeito.OCUPADO, 2, null, AcaoAutomatica.NENHUMA));

    mockMvc.perform(put("/sla/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prazoAlertaMin\":2,\"prazoAcaoMin\":null,\"acaoAutomatica\":\"NENHUMA\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.prazoAlertaMin").value(2));
  }

  @Test
  void atualizarInexistente404() throws Exception {
    UUID id = UUID.randomUUID();
    when(slaService.atualizar(eq(id), any(AtualizarSlaRequest.class)))
        .thenThrow(new RecursoNaoEncontradoException("SLA não encontrado"));

    mockMvc.perform(put("/sla/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prazoAlertaMin\":2,\"acaoAutomatica\":\"NENHUMA\"}"))
        .andExpect(status().isNotFound());
  }
}
