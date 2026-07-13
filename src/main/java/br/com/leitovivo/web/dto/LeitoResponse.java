package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;

import java.time.Instant;
import java.util.UUID;

public record LeitoResponse(
        UUID id,
        UUID unidadeId,
        String codigo,
        TipoLeito tipo,
        StatusLeito status,
        Long versao,
        boolean liberadoAutomaticamente,
        Instant dataUltimaAtualizacaoStatus) {
}
