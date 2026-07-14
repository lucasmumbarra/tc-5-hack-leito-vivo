package br.com.leitovivo.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "cartao_sus", nullable = false, length = 20)
    private String cartaoSus;

    protected Paciente() {
    }

    public Paciente(String nome, LocalDate dataNascimento, String cartaoSus) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.cartaoSus = cartaoSus;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getCartaoSus() {
        return cartaoSus;
    }
}
