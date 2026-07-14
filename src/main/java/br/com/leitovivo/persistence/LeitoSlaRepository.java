package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.StatusLeito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Consultas/atualizações de leito usadas pelo motor de SLA.
 * NÃO atualiza {@code status} — apenas flag de liberação automática e listagem.
 */
public interface LeitoSlaRepository extends JpaRepository<Leito, UUID> {

    @Query("select l from Leito l where l.status in :statuses")
    List<Leito> findByStatusIn(@Param("statuses") Collection<StatusLeito> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Leito l set l.liberadoAutomaticamente = true where l.id = :id")
    int marcarLiberadoAutomaticamente(@Param("id") UUID id);
}
