-- create_analytics.sql (UPDATED)
-- Generated to match biblioteca_db and the requested dashboard metrics.
SET SQL_SAFE_UPDATES=0;

DROP DATABASE IF EXISTS biblioteca_analytics;
CREATE DATABASE biblioteca_analytics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_analytics;

-- =========================================================
-- DIMENSÕES
-- =========================================================

CREATE TABLE dim_usuario (
    usuario_id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(100),
    email VARCHAR(100),
    sexo CHAR(1),
    dt_nascimento DATE,
    cidade VARCHAR(100),
    ativo BOOLEAN,
    role VARCHAR(50),
    dt_cadastro DATETIME
);

CREATE TABLE dim_livro (
    livro_id BIGINT PRIMARY KEY,
    titulo VARCHAR(255),
    ano_lancamento VARCHAR(10),
    editora VARCHAR(255),
    obra VARCHAR(255),
    num_total_licencas INT,
    sinopse TEXT,
    uri_img VARCHAR(255)
);

CREATE TABLE dim_autor (
    autor_id BIGINT PRIMARY KEY,
    nome_autor VARCHAR(255)
);

CREATE TABLE dim_tema (
    tema_id BIGINT PRIMARY KEY,
    nome_tema VARCHAR(255)
);

CREATE TABLE dim_date (
    dt DATE PRIMARY KEY,
    ano INT,
    mes INT,
    dia INT,
    semana_ano INT
);

-- Optional status dimensions to store both code and name
CREATE TABLE dim_status_emprestimo (
    cod_status INT PRIMARY KEY,
    nome_status VARCHAR(50)
);

CREATE TABLE dim_status_reserva (
    cod_status INT PRIMARY KEY,
    nome_status VARCHAR(50)
);

-- =========================================================
-- FATOS
-- =========================================================

CREATE TABLE fact_emprestimo (
    emprestimo_id BIGINT PRIMARY KEY,
    livro_id BIGINT,
    usuario_id VARCHAR(255),
    dt_inicio DATETIME,
    dt_fim DATETIME,
    num_renovacoes INT,
    status_emprestimo_cod INT,
    status_emprestimo_nome VARCHAR(50),
    duracao_dias INT,
    duracao_dias_atual INT,
    FOREIGN KEY (livro_id) REFERENCES dim_livro(livro_id)
);

CREATE TABLE fact_reserva (
    reserva_id BIGINT PRIMARY KEY,
    livro_id BIGINT,
    usuario_id VARCHAR(255),
    dt_inicio_reserva DATETIME,
    dt_fim_reserva DATETIME,
    status_reserva_cod INT,
    status_reserva_nome VARCHAR(50)
);

-- Ratings / satisfaction (optional if source has ratings)
CREATE TABLE fact_avaliacao (
    avaliacao_id BIGINT PRIMARY KEY,
    livro_id BIGINT,
    usuario_id VARCHAR(255),
    nota TINYINT,
    comentario TEXT,
    dt_avaliacao DATETIME
);

-- =========================================================
-- AGREGADOS / MATERIALIZADOS PARA DASHBOARD
-- =========================================================

CREATE TABLE agg_livro_popularidade (
    livro_id BIGINT PRIMARY KEY,
    titulo VARCHAR(255),
    total_emprestimos INT,
    total_reservas INT,
    total_ativos INT,
    exemplares_disponiveis INT
);

CREATE TABLE agg_autor_popularidade (
    autor_id BIGINT PRIMARY KEY,
    nome_autor VARCHAR(255),
    total_emprestimos INT
);

CREATE TABLE agg_tema_popularidade (
    tema_id BIGINT PRIMARY KEY,
    nome_tema VARCHAR(255),
    total_emprestimos INT
);

CREATE TABLE metric_emprestimo_summary (
    snapshot_dt DATETIME PRIMARY KEY,
    total_emp_ativos INT,
    tempo_medio_emprestimo_days FLOAT,
    taxa_renovacao FLOAT
);

CREATE TABLE metric_usuario_summary (
    snapshot_dt DATETIME PRIMARY KEY,
    total_usuarios INT,
    distrib_masc INT,
    distrib_fem INT,
    distrib_outros INT
);

CREATE TABLE agg_faixa_etaria_livros (
    faixa VARCHAR(20),
    livro_id BIGINT,
    titulo VARCHAR(255),
    total_emprestimos INT
);

CREATE TABLE agg_genero_favorito (
    sexo CHAR(1),
    tema_id BIGINT,
    nome_tema VARCHAR(255),
    total_emprestimos INT
);

CREATE TABLE agg_reservas_summary (
    snapshot_dt DATETIME PRIMARY KEY,
    total_reservas INT,
    total_canceladas INT
);

CREATE TABLE agg_livros_reservados (
    livro_id BIGINT PRIMARY KEY,
    titulo VARCHAR(255),
    total_reservas INT
);

CREATE TABLE agg_avaliacao_livro (
    livro_id BIGINT PRIMARY KEY,
    titulo VARCHAR(255),
    avg_nota FLOAT,
    total_avaliacoes INT
);

-- =========================================================
-- LOGS / OPERACIONAL
-- =========================================================

CREATE TABLE etl_run_log (
    run_dt DATETIME PRIMARY KEY,
    job_name VARCHAR(100),
    duration_seconds INT,
    status VARCHAR(20),
    rows_processed INT,
    notes TEXT
);

-- =========================================================
-- ÍNDICES PARA PERFORMANCE (recomendações)
-- =========================================================

CREATE INDEX idx_fact_emp_livro ON fact_emprestimo(livro_id);
CREATE INDEX idx_fact_emp_usuario ON fact_emprestimo(usuario_id);
CREATE INDEX idx_fact_res_livro ON fact_reserva(livro_id);
CREATE INDEX idx_dim_usuario_cidade ON dim_usuario(cidade);
CREATE INDEX idx_dim_usuario_dt_nasc ON dim_usuario(dt_nascimento);
CREATE INDEX idx_agg_livro_total_emp ON agg_livro_popularidade(total_emprestimos);

-- =========================================================
-- População inicial de dim_status (opcional mapping)
-- =========================================================
INSERT INTO dim_status_emprestimo (cod_status, nome_status) VALUES
(1,'ATIVO'), (2,'FINALIZADO'), (3,'CANCELADO')
ON DUPLICATE KEY UPDATE nome_status=VALUES(nome_status);

INSERT INTO dim_status_reserva (cod_status, nome_status) VALUES
(1,'ATIVA'), (2,'CANCELADA'), (3,'FINALIZADA')
ON DUPLICATE KEY UPDATE nome_status=VALUES(nome_status);

-- =========================================================
-- NOTAS:
-- - fact_emprestimo.duracao_dias: calculado como DATEDIFF(dt_fim, dt_inicio) (conforme solicitado)
-- - fact_emprestimo.duracao_dias_atual: opcional, pode ser calculado como DATEDIFF(CURDATE(), dt_inicio) para análises de empréstimos em aberto
-- - agg_livro_popularidade.exemplares_disponiveis será preenchido pelo ETL como: num_total_licencas - emprestimos_ativos_por_livro
-- - Recomendo rodar o ETL periodicamente (cron) e gravar no etl_run_log
-- =========================================================
