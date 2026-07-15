package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.persistence.entity.Leito;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeitoSlaRepository extends JpaRepository<Leito, UUID> {

  @Query("select l from Leito l where l.status in :statuses")
  List<Leito> findByStatusIn(@Param("statuses") Collection<StatusLeito> statuses);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Leito l set l.liberadoAutomaticamente = true where l.id = :id")
  int marcarLiberadoAutomaticamente(@Param("id") UUID id);
}
