-- V2: SLAs default do sistema (curinga unidade/tipo nulos)

INSERT INTO sla_status_leito (
    id, unidade_id, tipo_leito, status, prazo_alerta_min, prazo_acao_min, acao_automatica
) VALUES
    (gen_random_uuid(), NULL, NULL, 'OCUPADO',          28800, NULL, 'NENHUMA'),
    (gen_random_uuid(), NULL, NULL, 'EM_HIGIENIZACAO',    120,  240, 'LIBERAR_LEITO'),
    (gen_random_uuid(), NULL, NULL, 'RESERVADO',          360, NULL, 'NENHUMA'),
    (gen_random_uuid(), NULL, NULL, 'MANUTENCAO',       10080, NULL, 'NENHUMA');
