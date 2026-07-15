package br.com.leitovivo.persistence.entity;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "sla_status_leito")
public class SlaStatusLeito {

  @Id
  private UUID id;

  @Column(name = "unidade_id")
  private UUID unidadeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_leito", length = 40)
  private TipoLeito tipoLeito;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private StatusLeito status;

  @Column(name = "prazo_alerta_min", nullable = false)
  private int prazoAlertaMin;

  @Column(name = "prazo_acao_min")
  private Integer prazoAcaoMin;

  @Enumerated(EnumType.STRING)
  @Column(name = "acao_automatica", nullable = false, length = 40)
  private AcaoAutomatica acaoAutomatica;

  protected SlaStatusLeito() {
  }

  public SlaStatusLeito(
      UUID id,
      UUID unidadeId,
      TipoLeito tipoLeito,
      StatusLeito status,
      int prazoAlertaMin,
      Integer prazoAcaoMin,
      AcaoAutomatica acaoAutomatica) {
    this.id = id;
    this.unidadeId = unidadeId;
    this.tipoLeito = tipoLeito;
    this.status = status;
    this.prazoAlertaMin = prazoAlertaMin;
    this.prazoAcaoMin = prazoAcaoMin;
    this.acaoAutomatica = acaoAutomatica;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUnidadeId() {
    return unidadeId;
  }

  public TipoLeito getTipoLeito() {
    return tipoLeito;
  }

  public StatusLeito getStatus() {
    return status;
  }

  public int getPrazoAlertaMin() {
    return prazoAlertaMin;
  }

  public Integer getPrazoAcaoMin() {
    return prazoAcaoMin;
  }

  public AcaoAutomatica getAcaoAutomatica() {
    return acaoAutomatica;
  }

  public void atualizarPrazos(int prazoAlertaMin, Integer prazoAcaoMin, AcaoAutomatica acaoAutomatica) {
    this.prazoAlertaMin = prazoAlertaMin;
    this.prazoAcaoMin = prazoAcaoMin;
    this.acaoAutomatica = acaoAutomatica;
  }
}
