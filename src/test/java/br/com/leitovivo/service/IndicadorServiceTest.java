package br.com.leitovivo.service;

import br.com.leitovivo.domain.AutorAcao;
import br.com.leitovivo.domain.EventoLeito;
import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.domain.sla.SituacaoAlerta;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.AlertaIndicadorRepository;
import br.com.leitovivo.persistence.HistoricoIndicadorRepository;
import br.com.leitovivo.persistence.HistoricoStatusLeito;
import br.com.leitovivo.persistence.Internacao;
import br.com.leitovivo.persistence.InternacaoIndicadorRepository;
import br.com.leitovivo.persistence.Leito;
import br.com.leitovivo.persistence.LeitoBuscaIndicadorRepository;
import br.com.leitovivo.persistence.Paciente;
import br.com.leitovivo.persistence.StatusInternacao;
import br.com.leitovivo.persistence.Unidade;
import br.com.leitovivo.persistence.UnidadeRepository;
import br.com.leitovivo.web.dto.IndicadoresUnidadeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicadorServiceTest {

    @Mock
    private UnidadeRepository unidadeRepository;
    @Mock
    private LeitoBuscaIndicadorRepository leitoRepo;
    @Mock
    private InternacaoIndicadorRepository internacaoRepo;
    @Mock
    private HistoricoIndicadorRepository historicoRepo;
    @Mock
    private AlertaIndicadorRepository alertaRepo;

    private IndicadorService service;
    private UUID unidadeId;

    @BeforeEach
    void setUp() {
        service = new IndicadorService(unidadeRepository, leitoRepo, internacaoRepo, historicoRepo, alertaRepo);
        unidadeId = UUID.randomUUID();
    }

    @Test
    void taxaOcupacaoQuarentaPorCento() {
        when(unidadeRepository.existsById(unidadeId)).thenReturn(true);
        when(leitoRepo.countByUnidadeId(unidadeId)).thenReturn(10L);
        when(leitoRepo.countByUnidadeIdAndStatus(unidadeId, StatusLeito.OCUPADO)).thenReturn(4L);
        when(leitoRepo.countGroupedByStatus(unidadeId)).thenReturn(List.of(
                new Object[]{StatusLeito.OCUPADO, 4L},
                new Object[]{StatusLeito.LIVRE, 6L}));
        when(internacaoRepo.findByUnidadeIdAndStatus(unidadeId, StatusInternacao.ENCERRADA)).thenReturn(List.of());
        when(historicoRepo.findByUnidadeIdOrderByLeitoAndData(unidadeId)).thenReturn(List.of());
        when(alertaRepo.countByUnidadeIdAndSituacao(unidadeId, SituacaoAlerta.ABERTO)).thenReturn(0L);
        when(leitoRepo.countByUnidadeIdAndLiberadoAutomaticamenteTrue(unidadeId)).thenReturn(0L);

        IndicadoresUnidadeResponse r = service.calcular(unidadeId);

        assertEquals(40.0, r.taxaOcupacaoPercentual(), 0.001);
        assertEquals(4, r.contagemPorStatus().ocupado());
        assertNull(r.permanenciaMediaMinutos());
        assertNull(r.giroMedioMinutos());
    }

    @Test
    void unidadeSemLeitosTaxaZero() {
        when(unidadeRepository.existsById(unidadeId)).thenReturn(true);
        when(leitoRepo.countByUnidadeId(unidadeId)).thenReturn(0L);
        when(leitoRepo.countByUnidadeIdAndStatus(unidadeId, StatusLeito.OCUPADO)).thenReturn(0L);
        when(leitoRepo.countGroupedByStatus(unidadeId)).thenReturn(List.of());
        when(internacaoRepo.findByUnidadeIdAndStatus(unidadeId, StatusInternacao.ENCERRADA)).thenReturn(List.of());
        when(historicoRepo.findByUnidadeIdOrderByLeitoAndData(unidadeId)).thenReturn(List.of());
        when(alertaRepo.countByUnidadeIdAndSituacao(unidadeId, SituacaoAlerta.ABERTO)).thenReturn(0L);
        when(leitoRepo.countByUnidadeIdAndLiberadoAutomaticamenteTrue(unidadeId)).thenReturn(0L);

        assertEquals(0.0, service.calcular(unidadeId).taxaOcupacaoPercentual(), 0.001);
    }

    @Test
    void unidadeInexistente404() {
        when(unidadeRepository.existsById(unidadeId)).thenReturn(false);
        assertThrows(RecursoNaoEncontradoException.class, () -> service.calcular(unidadeId));
    }

    @Test
    void giroMedioCalculadoDoHistorico() throws Exception {
        when(unidadeRepository.existsById(unidadeId)).thenReturn(true);
        when(leitoRepo.countByUnidadeId(unidadeId)).thenReturn(1L);
        when(leitoRepo.countByUnidadeIdAndStatus(eq(unidadeId), eq(StatusLeito.OCUPADO))).thenReturn(0L);
        when(leitoRepo.countGroupedByStatus(unidadeId)).thenReturn(List.of());
        when(internacaoRepo.findByUnidadeIdAndStatus(unidadeId, StatusInternacao.ENCERRADA)).thenReturn(List.of());
        when(alertaRepo.countByUnidadeIdAndSituacao(unidadeId, SituacaoAlerta.ABERTO)).thenReturn(0L);
        when(leitoRepo.countByUnidadeIdAndLiberadoAutomaticamenteTrue(unidadeId)).thenReturn(1L);

        Unidade u = new Unidade("H", "C", "Sudeste", "G");
        setField(u, "id", unidadeId);
        Leito leito = new Leito(u, "L1", TipoLeito.UTI, StatusLeito.LIVRE, true, Instant.parse("2026-07-13T12:00:00Z"));
        setField(leito, "id", UUID.randomUUID());

        Instant t0 = Instant.parse("2026-07-13T10:00:00Z");
        Instant t1 = Instant.parse("2026-07-13T12:00:00Z"); // 120 min depois
        HistoricoStatusLeito h1 = new HistoricoStatusLeito(
                leito, StatusLeito.OCUPADO, StatusLeito.EM_HIGIENIZACAO,
                EventoLeito.REGISTRAR_ALTA, AutorAcao.USUARIO, null, t0);
        HistoricoStatusLeito h2 = new HistoricoStatusLeito(
                leito, StatusLeito.EM_HIGIENIZACAO, StatusLeito.LIVRE,
                EventoLeito.FINALIZAR_HIGIENIZACAO, AutorAcao.SISTEMA, "TIMEOUT_HIGIENIZACAO", t1);
        when(historicoRepo.findByUnidadeIdOrderByLeitoAndData(unidadeId)).thenReturn(List.of(h1, h2));

        IndicadoresUnidadeResponse r = service.calcular(unidadeId);
        assertEquals(120.0, r.giroMedioMinutos(), 0.001);
        assertEquals(1, r.leitosLiberadosAutomaticamente());
    }

    @Test
    void permanenciaMediaDeInternacoesEncerradas() throws Exception {
        when(unidadeRepository.existsById(unidadeId)).thenReturn(true);
        when(leitoRepo.countByUnidadeId(unidadeId)).thenReturn(1L);
        when(leitoRepo.countByUnidadeIdAndStatus(unidadeId, StatusLeito.OCUPADO)).thenReturn(0L);
        when(leitoRepo.countGroupedByStatus(unidadeId)).thenReturn(List.of());
        when(historicoRepo.findByUnidadeIdOrderByLeitoAndData(unidadeId)).thenReturn(List.of());
        when(alertaRepo.countByUnidadeIdAndSituacao(unidadeId, SituacaoAlerta.ABERTO)).thenReturn(2L);
        when(leitoRepo.countByUnidadeIdAndLiberadoAutomaticamenteTrue(unidadeId)).thenReturn(0L);

        Unidade u = new Unidade("H", "C", "Sudeste", "G");
        setField(u, "id", unidadeId);
        Leito leito = new Leito(u, "L1", TipoLeito.CLINICO, StatusLeito.LIVRE, false, Instant.now());
        setField(leito, "id", UUID.randomUUID());
        Paciente p = new Paciente("P", LocalDate.of(1990, 1, 1), "123");
        Internacao i = new Internacao(leito, p, Instant.parse("2026-07-01T00:00:00Z"));
        i.encerrar(Instant.parse("2026-07-03T00:00:00Z")); // 2 dias = 2880 min
        when(internacaoRepo.findByUnidadeIdAndStatus(unidadeId, StatusInternacao.ENCERRADA)).thenReturn(List.of(i));

        IndicadoresUnidadeResponse r = service.calcular(unidadeId);
        assertEquals(2880.0, r.permanenciaMediaMinutos(), 0.001);
        assertEquals(2, r.alertasAbertos());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
