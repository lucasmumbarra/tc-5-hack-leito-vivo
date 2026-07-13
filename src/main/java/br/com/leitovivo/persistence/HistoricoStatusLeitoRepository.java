package br.com.leitovivo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoStatusLeitoRepository extends JpaRepository<HistoricoStatusLeito, UUID> {

    List<HistoricoStatusLeito> findByLeitoIdOrderByDataHoraAsc(UUID leitoId);

    long countByLeitoId(UUID leitoId);
}
