package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.sla.AcaoAutomaticaSla;
import br.com.leitovivo.domain.sla.SituacaoAlerta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerta_leito")
public class AlertaLeito {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_id", nullable = false)
    private Leito leito;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_em_alerta", nullable = false, length = 40)
    private StatusLeito statusEmAlerta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAlerta situacao;

    @Column(name = "minutos_sem_atualizacao", nullable = false)
    private int minutosSemAtualizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao_executada", length = 40)
    private AcaoAutomaticaSla acaoExecutada;

    @Column(name = "data_abertura", nullable = false)
    private Instant dataAbertura;

    @Column(name = "data_resolucao")
    private Instant dataResolucao;

    @Column(name = "resolvido_por", length = 120)
    private String resolvidoPor;

    protected AlertaLeito() {
    }

    public AlertaLeito(
            UUID id,
            Leito leito,
            StatusLeito statusEmAlerta,
            SituacaoAlerta situacao,
            int minutosSemAtualizacao,
            Instant dataAbertura) {
        this.id = id;
        this.leito = leito;
        this.statusEmAlerta = statusEmAlerta;
        this.situacao = situacao;
        this.minutosSemAtualizacao = minutosSemAtualizacao;
        this.dataAbertura = dataAbertura;
    }

    public UUID getId() {
        return id;
    }

    public Leito getLeito() {
        return leito;
    }

    public StatusLeito getStatusEmAlerta() {
        return statusEmAlerta;
    }

    public SituacaoAlerta getSituacao() {
        return situacao;
    }

    public int getMinutosSemAtualizacao() {
        return minutosSemAtualizacao;
    }

    public AcaoAutomaticaSla getAcaoExecutada() {
        return acaoExecutada;
    }

    public Instant getDataAbertura() {
        return dataAbertura;
    }

    public Instant getDataResolucao() {
        return dataResolucao;
    }

    public String getResolvidoPor() {
        return resolvidoPor;
    }

    public void atualizarMinutosSemAtualizacao(int minutos) {
        this.minutosSemAtualizacao = minutos;
    }

    public void registrarAcaoExecutada(AcaoAutomaticaSla acao) {
        this.acaoExecutada = acao;
    }

    public void resolver(String responsavel, Instant instante) {
        this.situacao = SituacaoAlerta.RESOLVIDO;
        this.resolvidoPor = responsavel;
        this.dataResolucao = instante;
    }
}
