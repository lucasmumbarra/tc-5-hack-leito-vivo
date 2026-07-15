package br.com.leitovivo.persistence.entity;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "leito",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_leito_unidade_codigo",
        columnNames = {"unidade_id", "codigo"}
    )
)
public class Leito {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "unidade_id", nullable = false)
  private Unidade unidade;

  @Column(nullable = false, length = 50)
  private String codigo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private TipoLeito tipo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private StatusLeito status;

  @Version
  @Column(nullable = false)
  private Long versao;

  @Column(name = "liberado_automaticamente", nullable = false)
  private boolean liberadoAutomaticamente;

  @Column(name = "data_ultima_atualizacao_status", nullable = false)
  private Instant dataUltimaAtualizacaoStatus;

  protected Leito() {
  }

  public Leito(
      Unidade unidade,
      String codigo,
      TipoLeito tipo,
      StatusLeito status,
      boolean liberadoAutomaticamente,
      Instant dataUltimaAtualizacaoStatus) {
    this.unidade = unidade;
    this.codigo = codigo;
    this.tipo = tipo;
    this.status = status;
    this.liberadoAutomaticamente = liberadoAutomaticamente;
    this.dataUltimaAtualizacaoStatus = dataUltimaAtualizacaoStatus;
  }

  public UUID getId() {
    return id;
  }

  public Unidade getUnidade() {
    return unidade;
  }

  public String getCodigo() {
    return codigo;
  }

  public TipoLeito getTipo() {
    return tipo;
  }

  public StatusLeito getStatus() {
    return status;
  }

  public Long getVersao() {
    return versao;
  }

  public boolean isLiberadoAutomaticamente() {
    return liberadoAutomaticamente;
  }

  public Instant getDataUltimaAtualizacaoStatus() {
    return dataUltimaAtualizacaoStatus;
  }

  /**
   * Única mutação de status pós-criação. Deve ser chamada apenas por {@code LeitoService.transicionar}.
   */
  public void aplicarTransicao(StatusLeito novoStatus, Instant instante) {
    this.status = novoStatus;
    this.dataUltimaAtualizacaoStatus = instante;
  }
}
