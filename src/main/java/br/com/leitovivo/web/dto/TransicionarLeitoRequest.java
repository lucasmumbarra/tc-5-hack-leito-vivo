package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.AutorAcao;
import br.com.leitovivo.domain.EventoLeito;

public record TransicionarLeitoRequest(EventoLeito evento, AutorAcao autor, String motivo) {
}
