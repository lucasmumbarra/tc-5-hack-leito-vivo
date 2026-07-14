package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.sla.SituacaoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AlertaIndicadorRepository extends JpaRepository<AlertaLeito, UUID> {

    @Query("""
            select count(a) from AlertaLeito a
            where a.leito.unidade.id = :unidadeId
              and a.situacao = :situacao
            """)
    long countByUnidadeIdAndSituacao(
            @Param("unidadeId") UUID unidadeId,
            @Param("situacao") SituacaoAlerta situacao);
}
