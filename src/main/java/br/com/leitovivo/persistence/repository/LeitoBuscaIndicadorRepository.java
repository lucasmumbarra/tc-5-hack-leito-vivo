package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.Leito;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Consultas de leitura para busca compatível e indicadores (sem mutar status).
 */
public interface LeitoBuscaIndicadorRepository extends JpaRepository<Leito, UUID> {

    @Query("""
            select l from Leito l
            join fetch l.unidade u
            where u.regiao = :regiao
              and l.status = :status
              and l.tipo in :tipos
            """)
    List<Leito> findByRegiaoAndStatusAndTipoIn(
            @Param("regiao") String regiao,
            @Param("status") StatusLeito status,
            @Param("tipos") Collection<TipoLeito> tipos);

    long countByUnidadeId(UUID unidadeId);

    long countByUnidadeIdAndStatus(UUID unidadeId, StatusLeito status);

    long countByUnidadeIdAndLiberadoAutomaticamenteTrue(UUID unidadeId);

    @Query("""
            select l.status, count(l) from Leito l
            where l.unidade.id = :unidadeId
            group by l.status
            """)
    List<Object[]> countGroupedByStatus(@Param("unidadeId") UUID unidadeId);

    List<Leito> findByUnidadeId(UUID unidadeId);
}
