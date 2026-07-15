package br.com.leitovivo.exception;

import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;

public class TransicaoInvalidaException extends RuntimeException {

  private final StatusLeito statusAtual;
  private final EventoLeito evento;

  public TransicaoInvalidaException(StatusLeito statusAtual, EventoLeito evento) {
    super("Transição inválida: status=" + statusAtual + ", evento=" + evento);
    this.statusAtual = statusAtual;
    this.evento = evento;
  }

  public StatusLeito getStatusAtual() {
    return statusAtual;
  }

  public EventoLeito getEvento() {
    return evento;
  }
}
