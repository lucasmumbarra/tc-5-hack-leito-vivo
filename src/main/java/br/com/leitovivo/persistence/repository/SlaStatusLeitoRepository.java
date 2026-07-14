package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.SlaStatusLeito;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlaStatusLeitoRepository extends JpaRepository<SlaStatusLeito, UUID> {
}
