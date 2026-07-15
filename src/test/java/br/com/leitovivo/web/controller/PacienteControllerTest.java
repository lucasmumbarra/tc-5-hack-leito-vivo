package br.com.leitovivo.web.controller;

import br.com.leitovivo.service.PacienteService;
import br.com.leitovivo.web.dto.request.CriarPacienteRequest;
import br.com.leitovivo.web.dto.response.PacienteResponse;
import br.com.leitovivo.web.handler.GlobalExceptionHandler;
import java.time.LocalDate;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PacienteControllerTest {

  @Mock
  private PacienteService pacienteService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new PacienteController(pacienteService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void criarRetorna201ComId() throws Exception {
    UUID id = UUID.randomUUID();
    when(pacienteService.criar(any(CriarPacienteRequest.class)))
        .thenReturn(new PacienteResponse(id, "Maria", LocalDate.of(1980, 5, 12), "123"));

    mockMvc.perform(post("/pacientes")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nome":"Maria","dataNascimento":"1980-05-12","cartaoSus":"123"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }
}
