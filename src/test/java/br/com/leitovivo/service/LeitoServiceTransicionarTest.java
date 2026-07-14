package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.exception.TransicaoInvalidaException;
import br.com.leitovivo.persistence.entity.HistoricoStatusLeito;
import br.com.leitovivo.persistence.repository.HistoricoStatusLeitoRepository;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.repository.LeitoRepository;
import br.com.leitovivo.persistence.entity.Unidade;
import br.com.leitovivo.persistence.repository.UnidadeRepository;
import br.com.leitovivo.web.dto.response.LeitoResponse;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeitoServiceTransicionarTest {

    private static final Instant AGORA = Instant.parse("2026-07-13T15:00:00Z");

    @Mock
    private LeitoRepository leitoRepository;
    @Mock
    private UnidadeRepository unidadeRepository;
    @Mock
    private HistoricoStatusLeitoRepository historicoStatusLeitoRepository;

    private LeitoService leitoService;
    private Leito leito;

    @BeforeEach
    void setUp() throws Exception {
        leitoService = new LeitoService(
                leitoRepository, unidadeRepository, historicoStatusLeitoRepository,
                Clock.fixed(AGORA, ZoneOffset.UTC));
        Unidade unidade = new Unidade("H", "SP", "SE", "Geral");
        setField(unidade, "id", UUID.randomUUID());
        leito = new Leito(unidade, "L-1", TipoLeito.UTI, StatusLeito.LIVRE, false, Instant.parse("2026-07-13T10:00:00Z"));
        setField(leito, "id", UUID.randomUUID());
        setField(leito, "versao", 0L);
    }

    @Test
    void transicaoValidaAtualizaStatusEGravaHistorico() {
        when(leitoRepository.findById(leito.getId())).thenReturn(Optional.of(leito));
        when(historicoStatusLeitoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeitoResponse response = leitoService.transicionar(
                leito.getId(), EventoLeito.RESERVAR_LEITO, Autor.USUARIO, "reserva");

        assertEquals(StatusLeito.RESERVADO, response.status());
        assertEquals(AGORA, response.dataUltimaAtualizacaoStatus());

        ArgumentCaptor<HistoricoStatusLeito> captor = ArgumentCaptor.forClass(HistoricoStatusLeito.class);
        verify(historicoStatusLeitoRepository).save(captor.capture());
        HistoricoStatusLeito h = captor.getValue();
        assertEquals(StatusLeito.LIVRE, h.getStatusAnterior());
        assertEquals(StatusLeito.RESERVADO, h.getStatusNovo());
        assertEquals(EventoLeito.RESERVAR_LEITO, h.getEvento());
        assertEquals(Autor.USUARIO, h.getAutor());
    }

    @Test
    void transicaoInvalidaNaoGravaHistoricoNemAlteraStatus() {
        leito.aplicarTransicao(StatusLeito.OCUPADO, Instant.parse("2026-07-13T11:00:00Z"));
        when(leitoRepository.findById(leito.getId())).thenReturn(Optional.of(leito));

        assertThrows(TransicaoInvalidaException.class, () -> leitoService.transicionar(
                leito.getId(), EventoLeito.INTERNAR_PACIENTE, Autor.USUARIO, null));

        assertEquals(StatusLeito.OCUPADO, leito.getStatus());
        verify(historicoStatusLeitoRepository, never()).save(any());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
