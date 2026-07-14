package br.com.leitovivo.web.dto;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.domain.sla.AcaoAutomaticaSla;
import br.com.leitovivo.domain.sla.SituacaoAlerta;

import java.time.Instant;
import java.util.UUID;

public record AlertaResponse(
        UUID id,
        UUID leitoId,
        UUID unidadeId,
        TipoLeito tipoLeito,
        StatusLeito statusEmAlerta,
        SituacaoAlerta situacao,
        int minutosSemAtualizacao,
        AcaoAutomaticaSla acaoExecutada,
        Instant dataAbertura,
        Instant dataResolucao,
        String resolvidoPor) {
}
