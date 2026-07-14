-- V4: seed de demonstração (perfil demo) — timestamps relativos

DO $$
DECLARE
    v_unidade_id   UUID := gen_random_uuid();
    v_paciente_id  UUID := gen_random_uuid();
    v_leito_ocup   UUID := gen_random_uuid();
    v_leito_hig    UUID := gen_random_uuid();
BEGIN
    INSERT INTO unidade (id, nome, municipio, regiao, tipo)
    VALUES (v_unidade_id, 'Hospital Demo SLA', 'Belo Horizonte', 'Sudeste', 'Geral');

    INSERT INTO paciente (id, nome, data_nascimento, cartao_sus)
    VALUES (v_paciente_id, 'Paciente Fantasma Demo', DATE '1980-01-15', '999999999999999');

    -- Leito fantasma: OCUPADO há 25 dias
    INSERT INTO leito (
        id, unidade_id, codigo, tipo, status, versao,
        liberado_automaticamente, data_ultima_atualizacao_status
    ) VALUES (
        v_leito_ocup, v_unidade_id, 'DEMO-FANTASMA', 'UTI', 'OCUPADO', 0,
        false, now() - interval '25 days'
    );

    INSERT INTO internacao (id, leito_id, paciente_id, status, data_entrada, data_alta)
    VALUES (
        gen_random_uuid(), v_leito_ocup, v_paciente_id, 'ATIVA',
        now() - interval '25 days', NULL
    );

    -- Higienização esquecida: EM_HIGIENIZACAO há 6 horas
    INSERT INTO leito (
        id, unidade_id, codigo, tipo, status, versao,
        liberado_automaticamente, data_ultima_atualizacao_status
    ) VALUES (
        v_leito_hig, v_unidade_id, 'DEMO-HIGIENE', 'CLINICO', 'EM_HIGIENIZACAO', 0,
        false, now() - interval '6 hours'
    );
END $$;
