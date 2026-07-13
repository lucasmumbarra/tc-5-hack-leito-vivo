package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.AutorAcao;
import br.com.leitovivo.domain.EventoLeito;
import br.com.leitovivo.domain.StatusLeito;
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
@Table(name = "historico_status_leito")
public class HistoricoStatusLeito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_id", nullable = false)
    private Leito leito;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 40)
    private StatusLeito statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 40)
    private StatusLeito statusNovo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private EventoLeito evento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AutorAcao autor;

    @Column(length = 120)
    private String motivo;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected HistoricoStatusLeito() {
    }

    public HistoricoStatusLeito(
            Leito leito,
            StatusLeito statusAnterior,
            StatusLeito statusNovo,
            EventoLeito evento,
            AutorAcao autor,
            String motivo,
            Instant dataHora) {
        this.leito = leito;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.evento = evento;
        this.autor = autor;
        this.motivo = motivo;
        this.dataHora = dataHora;
    }

    public UUID getId() {
        return id;
    }

    public Leito getLeito() {
        return leito;
    }

    public StatusLeito getStatusAnterior() {
        return statusAnterior;
    }

    public StatusLeito getStatusNovo() {
        return statusNovo;
    }

    public EventoLeito getEvento() {
        return evento;
    }

    public AutorAcao getAutor() {
        return autor;
    }

    public String getMotivo() {
        return motivo;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
