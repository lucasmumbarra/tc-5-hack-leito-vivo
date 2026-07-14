package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.HistoricoStatusLeito;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HistoricoIndicadorRepository extends JpaRepository<HistoricoStatusLeito, UUID> {

    @Query("""
            select h from HistoricoStatusLeito h
            join fetch h.leito l
            where l.unidade.id = :unidadeId
            order by l.id, h.dataHora
            """)
    List<HistoricoStatusLeito> findByUnidadeIdOrderByLeitoAndData(@Param("unidadeId") UUID unidadeId);
}
