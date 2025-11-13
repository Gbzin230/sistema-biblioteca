-- Criação/seleção do banco de dados
CREATE DATABASE IF NOT EXISTS biblioteca_db;
USE biblioteca_db;

-- ==========================================
-- Ajustes de compatibilidade com o backend
-- Mantém o SQL do amigo e adiciona/compatibiliza colunas e tabelas
-- ==========================================

-- 1) TB_USUARIO: coluna 'role' (herança SINGLE_TABLE) e chave numérica auxiliar
ALTER TABLE TB_USUARIO
    ADD COLUMN role VARCHAR(20);

ALTER TABLE TB_USUARIO
    ADD COLUMN cod_usuario INT NOT NULL AUTO_INCREMENT,
    ADD UNIQUE KEY uk_tb_usuario_cod_usuario (cod_usuario);

-- 1.1) TB_USUARIO: coluna de ativação esperada pelo backend e limite de slots
ALTER TABLE TB_USUARIO
    ADD COLUMN flag_ativo BOOLEAN DEFAULT FALSE,
    ADD COLUMN limite_slots INT DEFAULT 3;

-- 2) Corrigir tipos dos FKs que referenciam 'cod_username' (amigo usa INT; tabela é VARCHAR)
ALTER TABLE TB_USUARIO_ROLE
    MODIFY COLUMN cod_username VARCHAR(255);

ALTER TABLE TB_EMPRESTIMO
    MODIFY COLUMN cod_username VARCHAR(255);

ALTER TABLE TB_RESERVA
    MODIFY COLUMN cod_username VARCHAR(255);

-- 3) Adicionar referência por 'cod_usuario' (usada pelo backend) e FKs correspondentes
ALTER TABLE TB_EMPRESTIMO
    ADD COLUMN cod_usuario INT,
    ADD INDEX idx_emp_cod_usuario (cod_usuario);

ALTER TABLE TB_RESERVA
    ADD COLUMN cod_usuario INT,
    ADD INDEX idx_res_cod_usuario (cod_usuario);

ALTER TABLE TB_EMPRESTIMO
    ADD CONSTRAINT fk_emp_usuario_cod FOREIGN KEY (cod_usuario) REFERENCES TB_USUARIO(cod_usuario);

ALTER TABLE TB_RESERVA
    ADD CONSTRAINT fk_res_usuario_cod FOREIGN KEY (cod_usuario) REFERENCES TB_USUARIO(cod_usuario);

-- 4) TB_LIVRO: adicionar colunas usadas pelo backend
ALTER TABLE TB_LIVRO
    ADD COLUMN status VARCHAR(20),
    ADD COLUMN num_ano_lancamento INT,
    ADD COLUMN txt_sinopse VARCHAR(2000);

-- 4.1) TB_EMPRESTIMO: adicionar coluna de data prevista
ALTER TABLE TB_EMPRESTIMO
    ADD COLUMN dt_prevista_devolucao DATE;

-- 4.2) TB_RESERVA: adicionar colunas de solicitação e fila
ALTER TABLE TB_RESERVA
    ADD COLUMN dt_solicitacao DATE,
    ADD COLUMN num_posicao_fila INT;

-- 5) Tabelas de domínio: adicionar coluna 'txt_nome' e popular a partir do nome existente
ALTER TABLE TB_AUTOR
    ADD COLUMN txt_nome VARCHAR(100);
UPDATE TB_AUTOR SET txt_nome = nome_autor WHERE txt_nome IS NULL;

ALTER TABLE TB_TEMA
    ADD COLUMN txt_nome VARCHAR(100);
UPDATE TB_TEMA SET txt_nome = nome_tema WHERE txt_nome IS NULL;

ALTER TABLE TB_TAG
    ADD COLUMN txt_nome VARCHAR(100);
UPDATE TB_TAG SET txt_nome = nome_tag WHERE txt_nome IS NULL;

ALTER TABLE TB_EDITORA
    ADD COLUMN txt_nome VARCHAR(100);
UPDATE TB_EDITORA SET txt_nome = nome_editora WHERE txt_nome IS NULL;

-- 6) Join table de Tags: criar tabela esperada pelo backend e migrar dados se existir a tabela do amigo
CREATE TABLE IF NOT EXISTS TB_LIVRO_TAG (
    cod_livro INT,
    cod_tag INT,
    PRIMARY KEY (cod_livro, cod_tag),
    FOREIGN KEY (cod_livro) REFERENCES TB_LIVRO(cod_livro),
    FOREIGN KEY (cod_tag) REFERENCES TB_TAG(cod_tag)
);

INSERT INTO TB_LIVRO_TAG (cod_livro, cod_tag)
SELECT cod_livro, cod_tag FROM TB_LIVRO_TAGS
ON DUPLICATE KEY UPDATE cod_livro = VALUES(cod_livro), cod_tag = VALUES(cod_tag);

-- Observações:
-- - Se sua versão do MySQL não suportar alguns ALTERs, execute-os individualmente.
-- - Mantivemos as estruturas originais e apenas adicionamos o que o backend espera.