package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.HistoricoStatusLeito;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoStatusLeitoRepository extends JpaRepository<HistoricoStatusLeito, UUID> {

    List<HistoricoStatusLeito> findByLeitoIdOrderByDataHoraAsc(UUID leitoId);

    long countByLeitoId(UUID leitoId);
}
