package br.com.leitovivo.persistence;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "internacao")
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_id", nullable = false)
    private Leito leito;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusInternacao status;

    @Column(name = "data_entrada", nullable = false)
    private Instant dataEntrada;

    @Column(name = "data_alta")
    private Instant dataAlta;

    protected Internacao() {
    }

    public Internacao(Leito leito, Paciente paciente, Instant dataEntrada) {
        this.leito = leito;
        this.paciente = paciente;
        this.status = StatusInternacao.ATIVA;
        this.dataEntrada = dataEntrada;
    }

    public void encerrar(Instant dataAlta) {
        this.status = StatusInternacao.ENCERRADA;
        this.dataAlta = dataAlta;
    }

    public UUID getId() {
        return id;
    }

    public Leito getLeito() {
        return leito;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public StatusInternacao getStatus() {
        return status;
    }

    public Instant getDataEntrada() {
        return dataEntrada;
    }

    public Instant getDataAlta() {
        return dataAlta;
    }
}
