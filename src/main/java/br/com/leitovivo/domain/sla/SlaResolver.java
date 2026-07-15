package br.com.leitovivo.domain.sla;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.model.SlaAplicavel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SlaResolver {

  private SlaResolver() {
  }

  public static Optional<SlaAplicavel> resolver(
      UUID unidadeId,
      TipoLeito tipo,
      StatusLeito status,
      List<SlaAplicavel> regras) {
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(regras, "regras");

    List<SlaAplicavel> doStatus = regras.stream()
        .filter(r -> r.status() == status)
        .toList();

    return primeiroCom(doStatus, unidadeId, tipo)
        .or(() -> primeiroCom(doStatus, unidadeId, null))
        .or(() -> primeiroCom(doStatus, null, tipo))
        .or(() -> primeiroCom(doStatus, null, null));
  }

  private static Optional<SlaAplicavel> primeiroCom(List<SlaAplicavel> regras, UUID unidadeId, TipoLeito tipo) {
    return regras.stream()
        .filter(r -> Objects.equals(r.unidadeId(), unidadeId) && Objects.equals(r.tipoLeito(), tipo))
        .findFirst();
  }
}
