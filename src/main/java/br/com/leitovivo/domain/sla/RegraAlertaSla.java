package br.com.leitovivo.domain.sla;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.enums.DecisaoSla;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class RegraAlertaSla {

  private RegraAlertaSla() {
  }

  public static DecisaoSla avaliar(
      StatusLeito statusAtual,
      Instant dataUltimaAtualizacaoStatus,
      int prazoAlertaMin,
      Integer prazoAcaoMin,
      AcaoAutomatica acaoAutomatica,
      Instant agora) {
    Objects.requireNonNull(statusAtual, "statusAtual");
    Objects.requireNonNull(dataUltimaAtualizacaoStatus, "dataUltimaAtualizacaoStatus");
    Objects.requireNonNull(acaoAutomatica, "acaoAutomatica");
    Objects.requireNonNull(agora, "agora");

    long minutos = Duration.between(dataUltimaAtualizacaoStatus, agora).toMinutes();
    if (minutos < prazoAlertaMin) {
      return DecisaoSla.NADA;
    }

    boolean liberar = statusAtual == StatusLeito.EM_HIGIENIZACAO
        && prazoAcaoMin != null
        && minutos >= prazoAcaoMin
        && acaoAutomatica == AcaoAutomatica.LIBERAR_LEITO;

    return liberar ? DecisaoSla.ABRIR_ALERTA_E_LIBERAR : DecisaoSla.ABRIR_ALERTA;
  }

  public static int minutosSemAtualizacao(Instant dataUltimaAtualizacaoStatus, Instant agora) {
    return (int) Duration.between(dataUltimaAtualizacaoStatus, agora).toMinutes();
  }
}
