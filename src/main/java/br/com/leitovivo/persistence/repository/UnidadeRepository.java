package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.Unidade;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {
}
