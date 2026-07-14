package br.com.leitovivo.web.dto.request;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;

public record TransicionarLeitoRequest(EventoLeito evento, Autor autor, String motivo) {
}
