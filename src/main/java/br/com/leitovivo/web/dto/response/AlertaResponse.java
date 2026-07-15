package br.com.leitovivo.web.dto.response;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;

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
    AcaoAutomatica acaoExecutada,
    Instant dataAbertura,
    Instant dataResolucao,
    String resolvidoPor) {
}
