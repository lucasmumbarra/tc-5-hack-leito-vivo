package br.com.leitovivo.web;

import br.com.leitovivo.domain.AutorAcao;
import br.com.leitovivo.domain.EventoLeito;
import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.service.LeitoService;
import br.com.leitovivo.web.dto.HistoricoStatusResponse;
import br.com.leitovivo.web.dto.LeitoResponse;
import br.com.leitovivo.web.dto.TransicionarLeitoRequest;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeitoTransicaoControllerTest {

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
    void patchStatusReservaLeito() throws Exception {
        UUID id = UUID.randomUUID();
        when(leitoService.transicionar(eq(id), any(TransicionarLeitoRequest.class)))
                .thenReturn(new LeitoResponse(id, UUID.randomUUID(), "UTI-01", TipoLeito.UTI,
                        StatusLeito.RESERVADO, 1L, false, Instant.parse("2026-07-13T17:00:00Z")));

        mockMvc.perform(patch("/leitos/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"evento":"RESERVAR_LEITO","autor":"USUARIO","motivo":"Transferência"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVADO"));
    }

    @Test
    void historicoCicloCompletoRetornaTresRegistros() throws Exception {
        UUID id = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-07-13T10:00:00Z");
        Instant t2 = Instant.parse("2026-07-13T11:00:00Z");
        Instant t3 = Instant.parse("2026-07-13T12:00:00Z");
        when(leitoService.listarHistorico(id)).thenReturn(List.of(
                new HistoricoStatusResponse(UUID.randomUUID(), StatusLeito.LIVRE, StatusLeito.OCUPADO,
                        EventoLeito.INTERNAR_PACIENTE, AutorAcao.USUARIO, null, t1),
                new HistoricoStatusResponse(UUID.randomUUID(), StatusLeito.OCUPADO, StatusLeito.EM_HIGIENIZACAO,
                        EventoLeito.REGISTRAR_ALTA, AutorAcao.USUARIO, null, t2),
                new HistoricoStatusResponse(UUID.randomUUID(), StatusLeito.EM_HIGIENIZACAO, StatusLeito.LIVRE,
                        EventoLeito.FINALIZAR_HIGIENIZACAO, AutorAcao.USUARIO, null, t3)
        ));

        mockMvc.perform(get("/leitos/" + id + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].statusNovo").value("OCUPADO"))
                .andExpect(jsonPath("$[1].statusNovo").value("EM_HIGIENIZACAO"))
                .andExpect(jsonPath("$[2].statusNovo").value("LIVRE"));
    }
}
