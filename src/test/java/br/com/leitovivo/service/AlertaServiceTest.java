package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.persistence.entity.AlertaLeito;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.entity.Unidade;
import br.com.leitovivo.persistence.repository.AlertaLeitoRepository;
import br.com.leitovivo.web.dto.request.ResolverAlertaRequest;
import br.com.leitovivo.web.dto.response.AlertaResponse;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

  private static final Instant AGORA = Instant.parse("2026-07-13T18:00:00Z");

  @Mock
  private AlertaLeitoRepository alertaLeitoRepository;

  private AlertaService alertaService;
  private Leito leito;
  private AlertaLeito alerta;

  @BeforeEach
  void setUp() throws Exception {
    alertaService = new AlertaService(alertaLeitoRepository, Clock.fixed(AGORA, ZoneOffset.UTC));
    Unidade unidade = new Unidade("H", "SP", "SE", "Geral");
    setField(unidade, "id", UUID.randomUUID());
    leito = new Leito(unidade, "L-1", TipoLeito.UTI, StatusLeito.OCUPADO, false, AGORA.minusSeconds(3600));
    setField(leito, "id", UUID.randomUUID());
    alerta = new AlertaLeito(UUID.randomUUID(), leito, StatusLeito.OCUPADO, SituacaoAlerta.ABERTO, 60, AGORA);
  }

  @Test
  void garantirAbertoInsereQuandoNaoExiste() {
    when(alertaLeitoRepository.insertAbertoIgnoreConflict(any(), eq(leito.getId()), eq("OCUPADO"), anyInt(), eq(AGORA)))
        .thenReturn(1);
    when(alertaLeitoRepository.findByLeitoIdAndStatusEmAlertaAndSituacao(
        leito.getId(), StatusLeito.OCUPADO, SituacaoAlerta.ABERTO))
        .thenReturn(Optional.of(alerta));

    AlertaLeito result = alertaService.garantirAberto(leito, StatusLeito.OCUPADO, 60, AGORA);
    assertEquals(alerta.getId(), result.getId());
  }

  @Test
  void garantirAbertoIdempotenteQuandoConflito() {
    when(alertaLeitoRepository.insertAbertoIgnoreConflict(any(), eq(leito.getId()), eq("OCUPADO"), anyInt(), eq(AGORA)))
        .thenReturn(0);
    when(alertaLeitoRepository.findByLeitoIdAndStatusEmAlertaAndSituacao(
        leito.getId(), StatusLeito.OCUPADO, SituacaoAlerta.ABERTO))
        .thenReturn(Optional.of(alerta));

    AlertaLeito result = alertaService.garantirAberto(leito, StatusLeito.OCUPADO, 90, AGORA);
    assertEquals(90, result.getMinutosSemAtualizacao());
  }

  @Test
  void resolverAlertaAberto() {
    when(alertaLeitoRepository.findById(alerta.getId())).thenReturn(Optional.of(alerta));

    AlertaResponse response = alertaService.resolver(
        alerta.getId(), new ResolverAlertaRequest("enfermeira.ana"));

    assertEquals(SituacaoAlerta.RESOLVIDO, response.situacao());
    assertEquals("enfermeira.ana", response.resolvidoPor());
    assertEquals(AGORA, response.dataResolucao());
  }

  @Test
  void resolverJaResolvidoLanca409() {
    alerta.resolver("alguem", AGORA.minusSeconds(10));
    when(alertaLeitoRepository.findById(alerta.getId())).thenReturn(Optional.of(alerta));

    assertThrows(ConflitoNegocioException.class, () ->
        alertaService.resolver(alerta.getId(), new ResolverAlertaRequest("outro")));
  }

  @Test
  void registrarAcaoExecutadaMantemAberto() {
    when(alertaLeitoRepository.findById(alerta.getId())).thenReturn(Optional.of(alerta));

    alertaService.registrarAcaoExecutada(alerta.getId(), AcaoAutomatica.LIBERAR_LEITO);

    assertEquals(AcaoAutomatica.LIBERAR_LEITO, alerta.getAcaoExecutada());
    assertEquals(SituacaoAlerta.ABERTO, alerta.getSituacao());
    verify(alertaLeitoRepository).findById(alerta.getId());
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }
}
