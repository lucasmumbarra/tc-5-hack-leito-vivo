package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.SlaStatusLeito;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaStatusLeitoRepository extends JpaRepository<SlaStatusLeito, UUID> {
}
