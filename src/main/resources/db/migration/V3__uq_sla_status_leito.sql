-- V3: unicidade da chave de resolução de SLA (NULLS NOT DISTINCT)

CREATE UNIQUE INDEX uq_sla_unidade_tipo_status
    ON sla_status_leito (unidade_id, tipo_leito, status)
    NULLS NOT DISTINCT;
