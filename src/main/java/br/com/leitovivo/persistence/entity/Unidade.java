package br.com.leitovivo.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "unidade")
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false, length = 120)
    private String municipio;

    @Column(nullable = false, length = 120)
    private String regiao;

    @Column(nullable = false, length = 80)
    private String tipo;

    protected Unidade() {
    }

    public Unidade(String nome, String municipio, String regiao, String tipo) {
        this.nome = nome;
        this.municipio = municipio;
        this.regiao = regiao;
        this.tipo = tipo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getRegiao() {
        return regiao;
    }

    public String getTipo() {
        return tipo;
    }
}
