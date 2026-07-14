package br.com.leitovivo.web.dto.response;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;

import java.time.Instant;
import java.util.UUID;

public record HistoricoStatusResponse(
        UUID id,
        StatusLeito statusAnterior,
        StatusLeito statusNovo,
        EventoLeito evento,
        Autor autor,
        String motivo,
        Instant dataHora) {
}
