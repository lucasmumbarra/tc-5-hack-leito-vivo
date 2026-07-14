package br.com.leitovivo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InternacaoIndicadorRepository extends JpaRepository<Internacao, UUID> {

    @Query("""
            select i from Internacao i
            join fetch i.leito l
            where l.unidade.id = :unidadeId
              and i.status = :status
            """)
    List<Internacao> findByUnidadeIdAndStatus(
            @Param("unidadeId") UUID unidadeId,
            @Param("status") StatusInternacao status);
}
