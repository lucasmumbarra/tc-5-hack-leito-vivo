package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.persistence.entity.Leito;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeitoRepository extends JpaRepository<Leito, UUID> {

  boolean existsByUnidadeIdAndCodigo(UUID unidadeId, String codigo);

  @Query("""
      select l from Leito l
      where (:unidadeId is null or l.unidade.id = :unidadeId)
        and (:tipo is null or l.tipo = :tipo)
        and (:status is null or l.status = :status)
      """)
  List<Leito> filtrar(
      @Param("unidadeId") UUID unidadeId,
      @Param("tipo") TipoLeito tipo,
      @Param("status") StatusLeito status);
}
