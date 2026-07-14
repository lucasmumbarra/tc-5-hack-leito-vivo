package br.com.leitovivo.service;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.persistence.Leito;
import br.com.leitovivo.persistence.LeitoBuscaIndicadorRepository;
import br.com.leitovivo.persistence.Unidade;
import br.com.leitovivo.web.dto.LeitoCompativelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaLeitoServiceTest {

    @Mock
    private LeitoBuscaIndicadorRepository repository;

    private BuscaLeitoService service;

    @BeforeEach
    void setUp() {
        service = new BuscaLeitoService(repository);
    }

    @Test
    void utiNaoRetornaEnfermaria() throws Exception {
        Unidade u = unidade("Sudeste");
        Leito uti = leito(u, "UTI-1", TipoLeito.UTI, StatusLeito.LIVRE);
        when(repository.findByRegiaoAndStatusAndTipoIn(
                eq("Sudeste"), eq(StatusLeito.LIVRE), eq(Set.of(TipoLeito.UTI))))
                .thenReturn(List.of(uti));

        List<LeitoCompativelResponse> result = service.buscarCompativeis(TipoLeito.UTI, "Sudeste");

        assertEquals(1, result.size());
        assertEquals(TipoLeito.UTI, result.getFirst().tipo());
    }

    @Test
    void listaVaziaQuandoNenhumCompativel() {
        when(repository.findByRegiaoAndStatusAndTipoIn(
                eq("Norte"), eq(StatusLeito.LIVRE), eq(Set.of(TipoLeito.UTI))))
                .thenReturn(List.of());

        assertTrue(service.buscarCompativeis(TipoLeito.UTI, "Norte").isEmpty());
    }

    @Test
    void necessidadeAusente422() {
        assertThrows(PayloadInvalidoException.class, () -> service.buscarCompativeis(null, "Sudeste"));
    }

    @Test
    void regiaoAusente422() {
        assertThrows(PayloadInvalidoException.class, () -> service.buscarCompativeis(TipoLeito.UTI, "  "));
    }

    private static Unidade unidade(String regiao) throws Exception {
        Unidade u = new Unidade("H", "Cidade", regiao, "Geral");
        setField(u, "id", UUID.randomUUID());
        return u;
    }

    private static Leito leito(Unidade u, String codigo, TipoLeito tipo, StatusLeito status) throws Exception {
        Leito l = new Leito(u, codigo, tipo, status, false, Instant.parse("2026-07-13T12:00:00Z"));
        setField(l, "id", UUID.randomUUID());
        return l;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
