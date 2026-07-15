package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.HistoricoStatusLeito;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoStatusLeitoRepository extends JpaRepository<HistoricoStatusLeito, UUID> {

  List<HistoricoStatusLeito> findByLeitoIdOrderByDataHoraAsc(UUID leitoId);

  long countByLeitoId(UUID leitoId);
}
