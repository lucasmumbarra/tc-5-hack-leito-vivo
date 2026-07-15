-- =============================================================================
-- V4: seed de demonstração (perfil demo) — timestamps relativos
-- Arquivo: classpath:db/migration-demo/V4__seed_demo.sql
--
-- Objetivo: massa legível para indicadores e demo Swagger/Postman.
-- Totais: 6 hospitais · 120 leitos · 88 OCUPADO · 6 RESERVADO · 8 EM_HIGIENIZACAO
--         · 1 MANUTENCAO · 17 LIVRE (3 com liberado_automaticamente=true)
-- =============================================================================

DO $$
DECLARE
    -- Tipos ciclados em cada hospital (7 tipos do schema)
    v_tipos           TEXT[] := ARRAY[
        'ENFERMARIA', 'CLINICO', 'UTI', 'UTI_NEONATAL',
        'PEDIATRICO', 'OBSTETRICO', 'ISOLAMENTO'
    ];

    -- Metadados dos 6 hospitais
    v_nomes           TEXT[] := ARRAY[
        'Hospital Demo Sudeste Crítico',
        'Hospital Demo Sudeste Referência',
        'Hospital Demo Grande SP Saudável',
        'Hospital Demo Grande SP Pressionado',
        'Hospital Demo Vale Equilibrado',
        'Hospital Demo Vale Observação'
    ];
    v_municipios      TEXT[] := ARRAY[
        'São Paulo', 'Campinas', 'Guarulhos', 'Osasco', 'São José dos Campos', 'Taubaté'
    ];
    v_regioes         TEXT[] := ARRAY[
        'Sudeste', 'Sudeste', 'Grande SP', 'Grande SP', 'Vale do Paraíba', 'Vale do Paraíba'
    ];
    -- Ocupados por hospital → taxas 95/75/55/80/70/65
    v_ocupados_alvo   INT[]  := ARRAY[19, 15, 11, 16, 14, 13];

    v_unidade_ids     UUID[] := ARRAY[]::UUID[];
    v_leito_ids       UUID[] := ARRAY[]::UUID[];
    v_leito_status    TEXT[] := ARRAY[]::TEXT[];
    v_leito_unidade   INT[]  := ARRAY[]::INT[];  -- índice 1..6 do hospital
    v_leito_auto      BOOLEAN[] := ARRAY[]::BOOLEAN[];
    v_leito_idade_min INT[]  := ARRAY[]::INT[];  -- minutos desde última atualização

    v_uid             UUID;
    v_lid             UUID;
    v_pid             UUID;
    v_h               INT;
    v_i               INT;
    v_status          TEXT;
    v_tipo            TEXT;
    v_codigo          TEXT;
    v_auto            BOOLEAN;
    v_idade_min       INT;
    v_entrada         TIMESTAMPTZ;
    v_alta            TIMESTAMPTZ;
    v_hig_inicio      TIMESTAMPTZ;
    v_hig_fim         TIMESTAMPTZ;
    v_dias_fantasma   INT;
    v_perm_dias       INT;
    v_offset_dias     INT;
    v_n_encerrada     INT;
    v_pac_seq         INT := 0;
    v_leito_idx       INT;
    v_count_livre     INT;
    v_count_hig       INT;
    v_count_res       INT;
    v_count_man       INT;
    v_count_auto      INT;
BEGIN
    -- =========================================================================
    -- BLOCO A: unidades (6 hospitais / 3 regiões)
    -- Alimenta: base para taxa/contagem/busca por região
    -- =========================================================================
    FOR v_h IN 1..6 LOOP
        v_uid := gen_random_uuid();
        INSERT INTO unidade (id, nome, municipio, regiao, tipo)
        VALUES (v_uid, v_nomes[v_h], v_municipios[v_h], v_regioes[v_h], 'Geral');
        v_unidade_ids := array_append(v_unidade_ids, v_uid);
    END LOOP;

    -- =========================================================================
    -- BLOCO B: leitos (20 por hospital = 120)
    -- Distribuição por hospital (ordem: OCUPADO*, RESERVADO, EM_HIGIENIZACAO,
    -- MANUTENCAO, LIVRE). Asterisco inclui fantasmas no crítico.
    -- =========================================================================
    FOR v_h IN 1..6 LOOP
        v_count_livre := CASE v_h
            WHEN 1 THEN 0
            WHEN 2 THEN 3
            WHEN 3 THEN 5
            WHEN 4 THEN 2
            WHEN 5 THEN 3
            WHEN 6 THEN 4
        END;
        v_count_hig := CASE v_h
            WHEN 1 THEN 0
            WHEN 2 THEN 1
            WHEN 3 THEN 2
            WHEN 4 THEN 1
            WHEN 5 THEN 2
            WHEN 6 THEN 2
        END;
        v_count_res := 1; -- todos têm 1 reserva (crítico = zumbi ~8h)
        v_count_man := CASE WHEN v_h = 3 THEN 1 ELSE 0 END;
        -- liberações automáticas históricas: hospitais 2, 3 e 4
        v_count_auto := CASE WHEN v_h IN (2, 3, 4) THEN 1 ELSE 0 END;

        FOR v_i IN 1..20 LOOP
            v_tipo := v_tipos[((v_i - 1) % 7) + 1];
            v_codigo := 'H' || v_h || '-L' || lpad(v_i::text, 2, '0');
            v_auto := false;
            v_idade_min := 180; -- default ~3h para leitos recentes

            -- Forçar UTI LIVRE em Grande SP (h3) e Vale (h5)
            IF v_h = 3 AND v_i = 3 THEN
                v_tipo := 'UTI';
            ELSIF v_h = 5 AND v_i = 3 THEN
                v_tipo := 'UTI';
            END IF;

            IF v_i <= v_ocupados_alvo[v_h] THEN
                v_status := 'OCUPADO';
                -- Fantasmas: 4 primeiros ocupados do hospital crítico (21–32 dias)
                IF v_h = 1 AND v_i <= 4 THEN
                    v_dias_fantasma := 20 + v_i * 3; -- 23, 26, 29, 32
                    v_idade_min := v_dias_fantasma * 24 * 60;
                ELSE
                    -- Ocupados “normais”: 1–10 dias
                    v_idade_min := (1 + ((v_h + v_i) % 10)) * 24 * 60;
                END IF;
            ELSIF v_i <= v_ocupados_alvo[v_h] + v_count_res THEN
                v_status := 'RESERVADO';
                IF v_h = 1 THEN
                    v_idade_min := 8 * 60; -- reserva zumbi ~8h
                ELSE
                    v_idade_min := 60 + (v_h * 15); -- reservas recentes (< 6h)
                END IF;
            ELSIF v_i <= v_ocupados_alvo[v_h] + v_count_res + v_count_hig THEN
                v_status := 'EM_HIGIENIZACAO';
                -- ABAIXO do prazo de alerta (120 min) e ação (240 min): 10–90 min
                v_idade_min := 10 + ((v_h * 7 + v_i * 11) % 81); -- 10..90
            ELSIF v_i <= v_ocupados_alvo[v_h] + v_count_res + v_count_hig + v_count_man THEN
                v_status := 'MANUTENCAO';
                v_idade_min := 2 * 24 * 60; -- 2 dias (< 7 dias de SLA)
            ELSE
                v_status := 'LIVRE';
                -- Marcar primeiras LIVRE dos hospitais 2/3/4 como liberação automática
                IF v_count_auto > 0
                   AND (v_i = v_ocupados_alvo[v_h] + v_count_res + v_count_hig + v_count_man + 1) THEN
                    v_auto := true;
                    v_idade_min := (2 + v_h) * 24 * 60; -- liberado há alguns dias
                ELSE
                    v_idade_min := 45 + (v_i * 5);
                END IF;
                -- Garantir UTI livre nos slots forçados
                IF (v_h = 3 OR v_h = 5) AND v_i = 3 THEN
                    -- se o índice 3 caiu em OCUPADO, já forçamos tipo acima;
                    -- aqui só garante que o leito UTI livre exista nos livres:
                    NULL;
                END IF;
            END IF;

            -- Ajuste: leitos 3 dos hospitais 3 e 5 devem ser UTI LIVRE
            IF (v_h = 3 OR v_h = 5) AND v_tipo = 'UTI' AND v_status <> 'LIVRE' THEN
                -- trocar tipo deste leito para CLINICO e marcar um LIVRE posterior como UTI
                v_tipo := 'CLINICO';
            END IF;

            v_lid := gen_random_uuid();
            INSERT INTO leito (
                id, unidade_id, codigo, tipo, status, versao,
                liberado_automaticamente, data_ultima_atualizacao_status
            ) VALUES (
                v_lid,
                v_unidade_ids[v_h],
                v_codigo,
                v_tipo,
                v_status,
                0,
                v_auto,
                now() - make_interval(mins => v_idade_min)
            );

            v_leito_ids := array_append(v_leito_ids, v_lid);
            v_leito_status := array_append(v_leito_status, v_status);
            v_leito_unidade := array_append(v_leito_unidade, v_h);
            v_leito_auto := array_append(v_leito_auto, v_auto);
            v_leito_idade_min := array_append(v_leito_idade_min, v_idade_min);
        END LOOP;

        -- Garantir UTI LIVRE: atualiza o primeiro LIVRE não-auto de h3/h5 para tipo UTI
        IF v_h IN (3, 5) THEN
            UPDATE leito l
            SET tipo = 'UTI'
            WHERE l.id = (
                SELECT id FROM leito
                WHERE unidade_id = v_unidade_ids[v_h]
                  AND status = 'LIVRE'
                  AND liberado_automaticamente = false
                ORDER BY codigo
                LIMIT 1
            );
        END IF;
    END LOOP;

    -- =========================================================================
    -- BLOCO C: pacientes + internações ATIVAS (1 por leito OCUPADO)
    -- Alimenta: taxa/contagem coerentes com RN03/RN04; fantasmas com data_alta nula
    -- =========================================================================
    FOR v_i IN 1..array_length(v_leito_ids, 1) LOOP
        IF v_leito_status[v_i] = 'OCUPADO' THEN
            v_pac_seq := v_pac_seq + 1;
            v_pid := gen_random_uuid();
            INSERT INTO paciente (id, nome, data_nascimento, cartao_sus)
            VALUES (
                v_pid,
                'Paciente Ativo ' || lpad(v_pac_seq::text, 3, '0'),
                (CURRENT_DATE - make_interval(years => 25 + (v_pac_seq % 40)))::date,
                lpad((700000000000000 + v_pac_seq)::text, 15, '0')
            );

            INSERT INTO internacao (id, leito_id, paciente_id, status, data_entrada, data_alta)
            VALUES (
                gen_random_uuid(),
                v_leito_ids[v_i],
                v_pid,
                'ATIVA',
                now() - make_interval(mins => v_leito_idade_min[v_i]),
                NULL
            );
        END IF;
    END LOOP;

    -- =========================================================================
    -- BLOCO D: histórico coerente dos leitos ATUAIS
    -- Alimenta: auditoria + base do giro; liberações SISTEMA para indicador
    -- =========================================================================
    FOR v_i IN 1..array_length(v_leito_ids, 1) LOOP
        v_lid := v_leito_ids[v_i];
        v_status := v_leito_status[v_i];
        v_idade_min := v_leito_idade_min[v_i];

        IF v_status = 'OCUPADO' THEN
            -- LIVRE -> RESERVADO -> OCUPADO
            INSERT INTO historico_status_leito (
                id, leito_id, status_anterior, status_novo, evento, autor, motivo, data_hora
            ) VALUES
                (gen_random_uuid(), v_lid, 'LIVRE', 'RESERVADO', 'RESERVAR_LEITO', 'USUARIO',
                 'Reserva para internação', now() - make_interval(mins => v_idade_min + 30)),
                (gen_random_uuid(), v_lid, 'RESERVADO', 'OCUPADO', 'INTERNAR_PACIENTE', 'USUARIO',
                 'Internação ativa', now() - make_interval(mins => v_idade_min));

        ELSIF v_status = 'RESERVADO' THEN
            INSERT INTO historico_status_leito (
                id, leito_id, status_anterior, status_novo, evento, autor, motivo, data_hora
            ) VALUES (
                gen_random_uuid(), v_lid, 'LIVRE', 'RESERVADO', 'RESERVAR_LEITO', 'USUARIO',
                CASE WHEN v_leito_unidade[v_i] = 1 THEN 'Reserva zumbi demo'
                     ELSE 'Reserva operacional' END,
                now() - make_interval(mins => v_idade_min)
            );

        ELSIF v_status = 'EM_HIGIENIZACAO' THEN
            -- ciclo recente: ocupação curta -> alta -> higienização atual (10–90 min)
            INSERT INTO historico_status_leito (
                id, leito_id, status_anterior, status_novo, evento, autor, motivo, data_hora
            ) VALUES
                (gen_random_uuid(), v_lid, 'RESERVADO', 'OCUPADO', 'INTERNAR_PACIENTE', 'USUARIO',
                 'Internação encerrada na alta',
                 now() - make_interval(mins => v_idade_min + 3 * 24 * 60)),
                (gen_random_uuid(), v_lid, 'OCUPADO', 'EM_HIGIENIZACAO', 'REGISTRAR_ALTA', 'USUARIO',
                 'Alta — aguardando higienização',
                 now() - make_interval(mins => v_idade_min));

        ELSIF v_status = 'MANUTENCAO' THEN
            INSERT INTO historico_status_leito (
                id, leito_id, status_anterior, status_novo, evento, autor, motivo, data_hora
            ) VALUES (
                gen_random_uuid(), v_lid, 'LIVRE', 'MANUTENCAO', 'ENVIAR_MANUTENCAO', 'USUARIO',
                'Manutenção preventiva', now() - make_interval(mins => v_idade_min)
            );

        ELSIF v_status = 'LIVRE' THEN
            IF v_leito_auto[v_i] THEN
                -- Liberação automática histórica (SISTEMA / TIMEOUT_HIGIENIZACAO)
                INSERT INTO historico_status_leito (
                    id, leito_id, status_anterior, status_novo, evento, autor, motivo, data_hora
                ) VALUES
                    (gen_random_uuid(), v_lid, 'OCUPADO', 'EM_HIGIENIZACAO', 'REGISTRAR_ALTA', 'USUARIO',
                     'Alta antes da liberação automática',
                     now() - make_interval(mins => v_idade_min + 300)),
                    (gen_random_uuid(), v_lid, 'EM_HIGIENIZACAO', 'LIVRE', 'FINALIZAR_HIGIENIZACAO', 'SISTEMA',
                     'TIMEOUT_HIGIENIZACAO',
                     now() - make_interval(mins => v_idade_min));
            ELSE
                INSERT INTO historico_status_leito (
                    id, leito_id, status_anterior, status_novo, evento, autor, motivo, data_hora
                ) VALUES
                    (gen_random_uuid(), v_lid, 'OCUPADO', 'EM_HIGIENIZACAO', 'REGISTRAR_ALTA', 'USUARIO',
                     'Alta — higienização concluída',
                     now() - make_interval(mins => v_idade_min + 90)),
                    (gen_random_uuid(), v_lid, 'EM_HIGIENIZACAO', 'LIVRE', 'FINALIZAR_HIGIENIZACAO', 'USUARIO',
                     'Higienização finalizada',
                     now() - make_interval(mins => v_idade_min));
            END IF;
        END IF;
    END LOOP;

    -- =========================================================================
    -- BLOCO E: alertas ABERTO — fantasmas + reserva zumbi (hospital crítico)
    -- Alimenta: alertasAbertos > 0 sem depender do 1º tick
    -- =========================================================================
    FOR v_i IN 1..array_length(v_leito_ids, 1) LOOP
        IF v_leito_unidade[v_i] = 1 AND v_leito_status[v_i] = 'OCUPADO'
           AND v_leito_idade_min[v_i] >= 21 * 24 * 60 THEN
            INSERT INTO alerta_leito (
                id, leito_id, status_em_alerta, situacao, minutos_sem_atualizacao,
                acao_executada, data_abertura, data_resolucao, resolvido_por
            ) VALUES (
                gen_random_uuid(),
                v_leito_ids[v_i],
                'OCUPADO',
                'ABERTO',
                v_leito_idade_min[v_i],
                NULL,
                now() - make_interval(mins => greatest(v_leito_idade_min[v_i] - 28800, 60)),
                NULL,
                NULL
            );
        ELSIF v_leito_unidade[v_i] = 1 AND v_leito_status[v_i] = 'RESERVADO' THEN
            INSERT INTO alerta_leito (
                id, leito_id, status_em_alerta, situacao, minutos_sem_atualizacao,
                acao_executada, data_abertura, data_resolucao, resolvido_por
            ) VALUES (
                gen_random_uuid(),
                v_leito_ids[v_i],
                'RESERVADO',
                'ABERTO',
                v_leito_idade_min[v_i],
                NULL,
                now() - make_interval(mins => greatest(v_leito_idade_min[v_i] - 360, 30)),
                NULL,
                NULL
            );
        END IF;
    END LOOP;

    -- =========================================================================
    -- BLOCO F: 48 internações ENCERRADAS (8 por hospital) + histórico de giro
    -- Alimenta: permanenciaMediaMinutos e giroMedioMinutos
    -- Permanência 2–12 dias; ciclos nos últimos 60 dias
    -- =========================================================================
    v_n_encerrada := 0;
    FOR v_h IN 1..6 LOOP
        FOR v_i IN 1..8 LOOP
            v_n_encerrada := v_n_encerrada + 1;
            v_pac_seq := v_pac_seq + 1;

            -- Escolhe um leito do hospital (cicla pelos 20)
            v_leito_idx := (v_h - 1) * 20 + ((v_i - 1) % 20) + 1;
            v_lid := v_leito_ids[v_leito_idx];

            v_perm_dias := 2 + ((v_h + v_i) % 11);          -- 2..12
            v_offset_dias := 5 + ((v_h * 3 + v_i * 5) % 45); -- alta entre ~5 e ~50 dias atrás
            v_alta := now() - make_interval(days => v_offset_dias);
            v_entrada := v_alta - make_interval(days => v_perm_dias);
            v_hig_inicio := v_alta;
            v_hig_fim := v_alta + make_interval(mins => 45 + ((v_h + v_i) % 120)); -- 45–164 min

            v_pid := gen_random_uuid();
            INSERT INTO paciente (id, nome, data_nascimento, cartao_sus)
            VALUES (
                v_pid,
                'Paciente Encerrado ' || lpad(v_n_encerrada::text, 3, '0'),
                (CURRENT_DATE - make_interval(years => 20 + (v_n_encerrada % 50)))::date,
                lpad((800000000000000 + v_n_encerrada)::text, 15, '0')
            );

            INSERT INTO internacao (id, leito_id, paciente_id, status, data_entrada, data_alta)
            VALUES (
                gen_random_uuid(), v_lid, v_pid, 'ENCERRADA', v_entrada, v_alta
            );

            -- Ciclo histórico passado para giro (não conflita com status atual)
            INSERT INTO historico_status_leito (
                id, leito_id, status_anterior, status_novo, evento, autor, motivo, data_hora
            ) VALUES
                (gen_random_uuid(), v_lid, 'RESERVADO', 'OCUPADO', 'INTERNAR_PACIENTE', 'USUARIO',
                 'Internação encerrada (histórico)', v_entrada),
                (gen_random_uuid(), v_lid, 'OCUPADO', 'EM_HIGIENIZACAO', 'REGISTRAR_ALTA', 'USUARIO',
                 'Alta — ciclo histórico', v_hig_inicio),
                (gen_random_uuid(), v_lid, 'EM_HIGIENIZACAO', 'LIVRE', 'FINALIZAR_HIGIENIZACAO', 'USUARIO',
                 'Higienização ciclo histórico', v_hig_fim);
        END LOOP;
    END LOOP;
END $$;
