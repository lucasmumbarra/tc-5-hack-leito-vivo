package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.AutorAcao;
import br.com.leitovivo.domain.EventoLeito;
import br.com.leitovivo.domain.StatusLeito;

import java.time.Instant;
import java.util.UUID;

public record HistoricoStatusResponse(
        UUID id,
        StatusLeito statusAnterior,
        StatusLeito statusNovo,
        EventoLeito evento,
        AutorAcao autor,
        String motivo,
        Instant dataHora) {
}
