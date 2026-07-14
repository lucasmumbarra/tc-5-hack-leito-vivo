package br.com.leitovivo.web.dto.response;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;

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
