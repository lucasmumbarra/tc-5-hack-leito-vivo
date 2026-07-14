package br.com.leitovivo.domain.sla;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolve o SLA aplicável em cascata, do mais específico ao default.
 * Não lê o relógio; opera apenas sobre a lista fornecida.
 */
public final class SlaResolver {

    private SlaResolver() {
    }

    /**
     * Ordem: (U,T,S) → (U,∅,S) → (∅,T,S) → (∅,∅,S).
     *
     * @return empty se não houver regra para o status
     */
    public static Optional<SlaRegra> resolver(
            UUID unidadeId,
            TipoLeito tipo,
            StatusLeito status,
            List<SlaRegra> regras) {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(regras, "regras");

        List<SlaRegra> doStatus = regras.stream()
                .filter(r -> r.status() == status)
                .toList();

        return primeiroCom(doStatus, unidadeId, tipo)
                .or(() -> primeiroCom(doStatus, unidadeId, null))
                .or(() -> primeiroCom(doStatus, null, tipo))
                .or(() -> primeiroCom(doStatus, null, null));
    }

    private static Optional<SlaRegra> primeiroCom(List<SlaRegra> regras, UUID unidadeId, TipoLeito tipo) {
        return regras.stream()
                .filter(r -> Objects.equals(r.unidadeId(), unidadeId) && Objects.equals(r.tipoLeito(), tipo))
                .findFirst();
    }
}
