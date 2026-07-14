package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.domain.sla.SituacaoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertaLeitoRepository extends JpaRepository<AlertaLeito, UUID> {

    Optional<AlertaLeito> findByLeitoIdAndStatusEmAlertaAndSituacao(
            UUID leitoId, StatusLeito statusEmAlerta, SituacaoAlerta situacao);

    @Query("""
            select a from AlertaLeito a
            join a.leito l
            where (:unidadeId is null or l.unidade.id = :unidadeId)
              and (:tipo is null or l.tipo = :tipo)
              and (:statusEmAlerta is null or a.statusEmAlerta = :statusEmAlerta)
              and (:situacao is null or a.situacao = :situacao)
            """)
    List<AlertaLeito> filtrar(
            @Param("unidadeId") UUID unidadeId,
            @Param("tipo") TipoLeito tipo,
            @Param("statusEmAlerta") StatusLeito statusEmAlerta,
            @Param("situacao") SituacaoAlerta situacao);

    /**
     * Insert idempotente respeitando uq_alerta_aberto_leito_status.
     * Retorna 1 se inseriu, 0 se conflito (alerta ABERTO já existe).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO alerta_leito (
                id, leito_id, status_em_alerta, situacao,
                minutos_sem_atualizacao, acao_executada,
                data_abertura, data_resolucao, resolvido_por
            ) VALUES (
                :id, :leitoId, :statusEmAlerta, 'ABERTO',
                :minutos, NULL,
                :dataAbertura, NULL, NULL
            )
            ON CONFLICT (leito_id, status_em_alerta) WHERE (situacao = 'ABERTO')
            DO NOTHING
            """, nativeQuery = true)
    int insertAbertoIgnoreConflict(
            @Param("id") UUID id,
            @Param("leitoId") UUID leitoId,
            @Param("statusEmAlerta") String statusEmAlerta,
            @Param("minutos") int minutos,
            @Param("dataAbertura") Instant dataAbertura);
}
