package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;
import br.com.leitovivo.persistence.entity.AlertaLeito;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
