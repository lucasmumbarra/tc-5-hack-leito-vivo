package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.persistence.entity.Internacao;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.entity.Paciente;
import br.com.leitovivo.persistence.entity.Unidade;
import br.com.leitovivo.persistence.enums.StatusInternacao;
import br.com.leitovivo.persistence.repository.InternacaoRepository;
import br.com.leitovivo.persistence.repository.LeitoRepository;
import br.com.leitovivo.persistence.repository.PacienteRepository;
import br.com.leitovivo.web.dto.request.CriarInternacaoRequest;
import br.com.leitovivo.web.dto.response.InternacaoResponse;
import br.com.leitovivo.web.dto.response.LeitoResponse;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternacaoServiceTest {

  private static final Instant AGORA = Instant.parse("2026-07-13T16:00:00Z");

  @Mock
  private InternacaoRepository internacaoRepository;
  @Mock
  private LeitoRepository leitoRepository;
  @Mock
  private PacienteRepository pacienteRepository;
  @Mock
  private LeitoService leitoService;

  private InternacaoService internacaoService;
  private Leito leito;
  private Paciente paciente;

  @BeforeEach
  void setUp() throws Exception {
    internacaoService = new InternacaoService(
        internacaoRepository, leitoRepository, pacienteRepository, leitoService,
        Clock.fixed(AGORA, ZoneOffset.UTC));

    Unidade unidade = new Unidade("H", "SP", "SE", "Geral");
    setField(unidade, "id", UUID.randomUUID());
    leito = new Leito(unidade, "L-1", TipoLeito.UTI, StatusLeito.LIVRE, false, AGORA);
    setField(leito, "id", UUID.randomUUID());
    paciente = new Paciente("Maria", LocalDate.of(1980, 1, 1), "123");
    setField(paciente, "id", UUID.randomUUID());
  }

  @Test
  void internarChamaFunilECriaInternacao() throws Exception {
    when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
    when(internacaoRepository.existsByPacienteIdAndStatus(paciente.getId(), StatusInternacao.ATIVA)).thenReturn(false);
    when(leitoRepository.findById(leito.getId())).thenReturn(Optional.of(leito));
    when(leitoService.transicionar(eq(leito.getId()), eq(EventoLeito.INTERNAR_PACIENTE), any(), any()))
        .thenReturn(new LeitoResponse(leito.getId(), leito.getUnidade().getId(), "L-1",
            TipoLeito.UTI, StatusLeito.OCUPADO, 1L, false, AGORA));
    when(internacaoRepository.save(any(Internacao.class))).thenAnswer(inv -> {
      Internacao i = inv.getArgument(0);
      setField(i, "id", UUID.randomUUID());
      return i;
    });

    InternacaoResponse response = internacaoService.internar(
        new CriarInternacaoRequest(leito.getId(), paciente.getId(), "eletiva"));

    assertEquals(StatusInternacao.ATIVA, response.status());
    verify(leitoService).transicionar(eq(leito.getId()), eq(EventoLeito.INTERNAR_PACIENTE), any(), any());
  }

  @Test
  void pacienteJaInternadoLanca409() {
    when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
    when(internacaoRepository.existsByPacienteIdAndStatus(paciente.getId(), StatusInternacao.ATIVA)).thenReturn(true);

    assertThrows(ConflitoNegocioException.class, () -> internacaoService.internar(
        new CriarInternacaoRequest(leito.getId(), paciente.getId(), null)));
  }

  @Test
  void leitoOcupadoBloqueiaInternacao() {
    leito.aplicarTransicao(StatusLeito.OCUPADO, AGORA);
    when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
    when(internacaoRepository.existsByPacienteIdAndStatus(paciente.getId(), StatusInternacao.ATIVA)).thenReturn(false);
    when(leitoRepository.findById(leito.getId())).thenReturn(Optional.of(leito));

    assertThrows(ConflitoNegocioException.class, () -> internacaoService.internar(
        new CriarInternacaoRequest(leito.getId(), paciente.getId(), null)));
  }

  @Test
  void altaDuplicadaLanca409() throws Exception {
    Internacao internacao = new Internacao(leito, paciente, AGORA);
    setField(internacao, "id", UUID.randomUUID());
    internacao.encerrar(AGORA);
    when(internacaoRepository.findById(internacao.getId())).thenReturn(Optional.of(internacao));

    assertThrows(ConflitoNegocioException.class,
        () -> internacaoService.registrarAlta(internacao.getId(), null));
  }

  @Test
  void altaChamaFunilRegistrarAlta() throws Exception {
    Internacao internacao = new Internacao(leito, paciente, AGORA);
    setField(internacao, "id", UUID.randomUUID());
    when(internacaoRepository.findById(internacao.getId())).thenReturn(Optional.of(internacao));
    when(leitoService.transicionar(eq(leito.getId()), eq(EventoLeito.REGISTRAR_ALTA), any(), any()))
        .thenReturn(new LeitoResponse(leito.getId(), leito.getUnidade().getId(), "L-1",
            TipoLeito.UTI, StatusLeito.EM_HIGIENIZACAO, 1L, false, AGORA));

    InternacaoResponse response = internacaoService.registrarAlta(internacao.getId(), "alta clínica");

    assertEquals(StatusInternacao.ENCERRADA, response.status());
    assertEquals(AGORA, response.dataAlta());
    verify(leitoService).transicionar(eq(leito.getId()), eq(EventoLeito.REGISTRAR_ALTA), any(), any());
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }
}
