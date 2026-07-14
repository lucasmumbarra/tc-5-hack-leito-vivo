package br.com.leitovivo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlaStatusLeitoRepository extends JpaRepository<SlaStatusLeito, UUID> {
}
