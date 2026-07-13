-- V1: schema completo do domínio LeitoVivo

CREATE TABLE unidade (
    id          UUID PRIMARY KEY,
    nome        VARCHAR(200) NOT NULL,
    municipio   VARCHAR(120) NOT NULL,
    regiao      VARCHAR(120) NOT NULL,
    tipo        VARCHAR(80)  NOT NULL
);

CREATE TABLE leito (
    id                              UUID PRIMARY KEY,
    unidade_id                      UUID         NOT NULL REFERENCES unidade (id),
    codigo                          VARCHAR(50)  NOT NULL,
    tipo                            VARCHAR(40)  NOT NULL,
    status                          VARCHAR(40)  NOT NULL,
    versao                          BIGINT       NOT NULL DEFAULT 0,
    liberado_automaticamente        BOOLEAN      NOT NULL DEFAULT false,
    data_ultima_atualizacao_status  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_leito_unidade_codigo UNIQUE (unidade_id, codigo),
    CONSTRAINT ck_leito_status CHECK (status IN (
        'LIVRE', 'RESERVADO', 'OCUPADO', 'EM_HIGIENIZACAO', 'MANUTENCAO'
    )),
    CONSTRAINT ck_leito_tipo CHECK (tipo IN (
        'ENFERMARIA', 'CLINICO', 'UTI', 'UTI_NEONATAL',
        'PEDIATRICO', 'OBSTETRICO', 'ISOLAMENTO'
    ))
);

CREATE INDEX idx_leito_status_atualizacao
    ON leito (status, data_ultima_atualizacao_status);

CREATE TABLE paciente (
    id               UUID PRIMARY KEY,
    nome             VARCHAR(200) NOT NULL,
    data_nascimento  DATE         NOT NULL,
    cartao_sus       VARCHAR(20)  NOT NULL
);

CREATE TABLE internacao (
    id           UUID PRIMARY KEY,
    leito_id     UUID        NOT NULL REFERENCES leito (id),
    paciente_id  UUID        NOT NULL REFERENCES paciente (id),
    status       VARCHAR(20) NOT NULL,
    data_entrada TIMESTAMPTZ NOT NULL,
    data_alta    TIMESTAMPTZ,
    CONSTRAINT ck_internacao_status CHECK (status IN ('ATIVA', 'ENCERRADA'))
);

CREATE TABLE historico_status_leito (
    id              UUID PRIMARY KEY,
    leito_id        UUID         NOT NULL REFERENCES leito (id),
    status_anterior VARCHAR(40),
    status_novo     VARCHAR(40)  NOT NULL,
    evento          VARCHAR(60)  NOT NULL,
    autor           VARCHAR(20)  NOT NULL,
    motivo          VARCHAR(120),
    data_hora       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_historico_autor CHECK (autor IN ('USUARIO', 'SISTEMA'))
);

CREATE INDEX idx_historico_leito_data
    ON historico_status_leito (leito_id, data_hora);

CREATE TABLE alerta_leito (
    id                       UUID PRIMARY KEY,
    leito_id                 UUID         NOT NULL REFERENCES leito (id),
    status_em_alerta         VARCHAR(40)  NOT NULL,
    situacao                 VARCHAR(20)  NOT NULL,
    minutos_sem_atualizacao  INTEGER      NOT NULL,
    acao_executada           VARCHAR(40),
    data_abertura            TIMESTAMPTZ  NOT NULL,
    data_resolucao           TIMESTAMPTZ,
    resolvido_por            VARCHAR(120),
    CONSTRAINT ck_alerta_situacao CHECK (situacao IN ('ABERTO', 'RESOLVIDO'))
);

CREATE UNIQUE INDEX uq_alerta_aberto_leito_status
    ON alerta_leito (leito_id, status_em_alerta)
    WHERE situacao = 'ABERTO';

CREATE TABLE sla_status_leito (
    id                UUID PRIMARY KEY,
    unidade_id        UUID REFERENCES unidade (id),
    tipo_leito        VARCHAR(40),
    status            VARCHAR(40) NOT NULL,
    prazo_alerta_min  INTEGER     NOT NULL,
    prazo_acao_min    INTEGER,
    acao_automatica   VARCHAR(40) NOT NULL,
    CONSTRAINT ck_sla_status CHECK (status IN (
        'LIVRE', 'RESERVADO', 'OCUPADO', 'EM_HIGIENIZACAO', 'MANUTENCAO'
    )),
    CONSTRAINT ck_sla_acao CHECK (acao_automatica IN ('NENHUMA', 'LIBERAR_LEITO'))
);
