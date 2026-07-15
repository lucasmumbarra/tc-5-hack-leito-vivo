package br.com.leitovivo.web.handler;

import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void payloadInvalidoRetorna422() throws Exception {
    mockMvc.perform(post("/__probe__/invalid")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.detail").exists());
  }

  @Test
  void recursoNaoEncontradoRetorna404() throws Exception {
    mockMvc.perform(get("/__probe__/missing"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").exists());
  }

  @RestController
  static class ProbeController {

    @PostMapping("/__probe__/invalid")
    void invalid(@RequestBody(required = false) String body) {
      throw new PayloadInvalidoException("nome é obrigatório");
    }

    @GetMapping("/__probe__/missing")
    void missing() {
      throw new RecursoNaoEncontradoException("Unidade não encontrada");
    }
  }
}
