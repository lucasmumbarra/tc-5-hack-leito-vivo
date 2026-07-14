package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.Unidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {
}
