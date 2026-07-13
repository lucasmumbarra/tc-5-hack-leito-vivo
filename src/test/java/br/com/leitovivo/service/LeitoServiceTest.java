package br.com.leitovivo.service;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.HistoricoStatusLeitoRepository;
import br.com.leitovivo.persistence.Leito;
import br.com.leitovivo.persistence.LeitoRepository;
import br.com.leitovivo.persistence.Unidade;
import br.com.leitovivo.persistence.UnidadeRepository;
import br.com.leitovivo.web.dto.CriarLeitoRequest;
import br.com.leitovivo.web.dto.LeitoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeitoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-07-13T14:00:00Z");

    @Mock
    private LeitoRepository leitoRepository;
    @Mock
    private UnidadeRepository unidadeRepository;
    @Mock
    private HistoricoStatusLeitoRepository historicoStatusLeitoRepository;

    private LeitoService leitoService;
    private Unidade unidade;

    @BeforeEach
    void setUp() throws Exception {
        Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
        leitoService = new LeitoService(leitoRepository, unidadeRepository, historicoStatusLeitoRepository, clock);
        unidade = new Unidade("Hospital", "SP", "Sudeste", "Geral");
        setField(unidade, "id", UUID.randomUUID());
    }

    @Test
    void criarForcaStatusLivreETimestamp() throws Exception {
        when(unidadeRepository.findById(unidade.getId())).thenReturn(Optional.of(unidade));
        when(leitoRepository.existsByUnidadeIdAndCodigo(unidade.getId(), "UTI-01")).thenReturn(false);
        when(leitoRepository.save(any(Leito.class))).thenAnswer(inv -> persistido(inv.getArgument(0)));

        LeitoResponse response = leitoService.criar(
                new CriarLeitoRequest(unidade.getId(), "UTI-01", TipoLeito.UTI));

        assertEquals(StatusLeito.LIVRE, response.status());
        assertFalse(response.liberadoAutomaticamente());
        assertEquals(AGORA, response.dataUltimaAtualizacaoStatus());

        ArgumentCaptor<Leito> captor = ArgumentCaptor.forClass(Leito.class);
        verify(leitoRepository).save(captor.capture());
        assertEquals(StatusLeito.LIVRE, captor.getValue().getStatus());
    }

    @Test
    void codigoDuplicadoLanca409() {
        when(unidadeRepository.findById(unidade.getId())).thenReturn(Optional.of(unidade));
        when(leitoRepository.existsByUnidadeIdAndCodigo(unidade.getId(), "UTI-01")).thenReturn(true);

        assertThrows(ConflitoNegocioException.class, () -> leitoService.criar(
                new CriarLeitoRequest(unidade.getId(), "UTI-01", TipoLeito.UTI)));
    }

    @Test
    void unidadeInexistenteLanca404() {
        UUID id = UUID.randomUUID();
        when(unidadeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> leitoService.criar(
                new CriarLeitoRequest(id, "UTI-01", TipoLeito.UTI)));
    }

    @Test
    void filtroCombinadoDelegaAoRepositorio() {
        when(leitoRepository.filtrar(unidade.getId(), TipoLeito.UTI, StatusLeito.LIVRE)).thenReturn(List.of());

        List<LeitoResponse> result = leitoService.listar(unidade.getId(), TipoLeito.UTI, StatusLeito.LIVRE);

        assertTrue(result.isEmpty());
        verify(leitoRepository).filtrar(unidade.getId(), TipoLeito.UTI, StatusLeito.LIVRE);
    }

    private static Leito persistido(Leito leito) throws Exception {
        setField(leito, "id", UUID.randomUUID());
        setField(leito, "versao", 0L);
        return leito;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
