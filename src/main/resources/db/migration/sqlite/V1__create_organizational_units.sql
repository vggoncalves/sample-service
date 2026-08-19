CREATE TABLE unidade_organizacional (
    id VARCHAR(36) PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    sigla VARCHAR(30),
    descricao VARCHAR(500),
    tipo VARCHAR(30) NOT NULL,
    unidade_pai_id VARCHAR(36),
    email_contato VARCHAR(254),
    telefone VARCHAR(16),
    ativa BOOLEAN NOT NULL DEFAULT 1,
    criado_em VARCHAR(35) NOT NULL,
    criado_por VARCHAR(255) NOT NULL,
    atualizado_em VARCHAR(35) NOT NULL,
    atualizado_por VARCHAR(255) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_unidade_organizacional_codigo UNIQUE (codigo),
    CONSTRAINT fk_unidade_organizacional_pai
        FOREIGN KEY (unidade_pai_id) REFERENCES unidade_organizacional (id) ON DELETE RESTRICT,
    CONSTRAINT ck_unidade_organizacional_pai CHECK (unidade_pai_id IS NULL OR id <> unidade_pai_id),
    CONSTRAINT ck_unidade_organizacional_tipo CHECK (tipo IN (
        'INSTITUICAO', 'DIRETORIA', 'DEPARTAMENTO', 'COORDENACAO',
        'REGIONAL', 'FILIAL', 'UNIDADE_ATENDIMENTO', 'OUTRA'
    )),
    CONSTRAINT ck_unidade_organizacional_ativa CHECK (ativa IN (0, 1)),
    CONSTRAINT ck_unidade_organizacional_versao CHECK (versao >= 0)
);

CREATE INDEX ix_unidade_organizacional_pai ON unidade_organizacional (unidade_pai_id);
CREATE INDEX ix_unidade_organizacional_ativa_nome ON unidade_organizacional (ativa, nome);
CREATE INDEX ix_unidade_organizacional_tipo ON unidade_organizacional (tipo);
CREATE INDEX ix_unidade_organizacional_nome ON unidade_organizacional (nome);
