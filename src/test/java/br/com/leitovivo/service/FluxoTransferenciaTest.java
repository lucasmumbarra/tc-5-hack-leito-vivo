package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.entity.Unidade;
import br.com.leitovivo.persistence.repository.LeitoBuscaIndicadorRepository;
import br.com.leitovivo.web.dto.request.CriarInternacaoRequest;
import br.com.leitovivo.web.dto.response.InternacaoResponse;
import br.com.leitovivo.web.dto.response.LeitoCompativelResponse;
import br.com.leitovivo.web.dto.response.LeitoResponse;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FluxoTransferenciaTest {

  @Mock
  private LeitoBuscaIndicadorRepository buscaRepo;
  @Mock
  private LeitoService leitoService;
  @Mock
  private InternacaoService internacaoService;

  private BuscaLeitoService buscaLeitoService;
  private UUID leitoId;
  private UUID unidadeId;
  private UUID pacienteId;

  @BeforeEach
  void setUp() throws Exception {
    buscaLeitoService = new BuscaLeitoService(buscaRepo);
    leitoId = UUID.randomUUID();
    unidadeId = UUID.randomUUID();
    pacienteId = UUID.randomUUID();
  }

  @Test
  void buscarReservarInternar() throws Exception {
    Unidade u = new Unidade("H", "Cidade", "Sudeste", "Geral");
    setField(u, "id", unidadeId);
    Leito leitoLivre = new Leito(u, "UTI-TX", TipoLeito.UTI, StatusLeito.LIVRE, false,
        Instant.parse("2026-07-13T10:00:00Z"));
    setField(leitoLivre, "id", leitoId);

    when(buscaRepo.findByRegiaoAndStatusAndTipoIn(
        eq("Sudeste"), eq(StatusLeito.LIVRE), eq(Set.of(TipoLeito.UTI))))
        .thenReturn(List.of(leitoLivre));

    List<LeitoCompativelResponse> encontrados =
        buscaLeitoService.buscarCompativeis(TipoLeito.UTI, "Sudeste");
    assertEquals(1, encontrados.size());
    assertEquals(leitoId, encontrados.getFirst().id());

    when(leitoService.transicionar(
        leitoId, EventoLeito.RESERVAR_LEITO, Autor.USUARIO, "transferencia"))
        .thenReturn(new LeitoResponse(
            leitoId, unidadeId, "UTI-TX", TipoLeito.UTI, StatusLeito.RESERVADO,
            1L, false, Instant.parse("2026-07-13T11:00:00Z")));

    LeitoResponse reservado = leitoService.transicionar(
        leitoId, EventoLeito.RESERVAR_LEITO, Autor.USUARIO, "transferencia");
    assertEquals(StatusLeito.RESERVADO, reservado.status());

    when(buscaRepo.findByRegiaoAndStatusAndTipoIn(
        eq("Sudeste"), eq(StatusLeito.LIVRE), eq(Set.of(TipoLeito.UTI))))
        .thenReturn(List.of());
    assertTrue(buscaLeitoService.buscarCompativeis(TipoLeito.UTI, "Sudeste").isEmpty(),
        "RESERVADO não aparece na busca");

    when(internacaoService.internar(any(CriarInternacaoRequest.class)))
        .thenReturn(new InternacaoResponse(
            UUID.randomUUID(), leitoId, pacienteId,
            br.com.leitovivo.persistence.enums.StatusInternacao.ATIVA,
            Instant.parse("2026-07-13T12:00:00Z"), null));

    InternacaoResponse internacao = internacaoService.internar(
        new CriarInternacaoRequest(leitoId, pacienteId, "chegada"));
    assertEquals(leitoId, internacao.leitoId());

    verify(leitoService).transicionar(
        leitoId, EventoLeito.RESERVAR_LEITO, Autor.USUARIO, "transferencia");
    verify(internacaoService).internar(any(CriarInternacaoRequest.class));
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    f.set(target, value);
  }
}
