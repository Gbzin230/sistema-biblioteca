-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS biblioteca_db;
USE biblioteca_db;

-- ==========================
-- Tabelas de apoio e controle
-- ==========================

CREATE TABLE IF NOT EXISTS TB_ROLE (
    cod_role INT AUTO_INCREMENT PRIMARY KEY,
    nome_role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_STATUS_USUARIO (
    cod_status INT AUTO_INCREMENT PRIMARY KEY,
    nome_status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_STATUS_RESERVA (
    cod_status INT AUTO_INCREMENT PRIMARY KEY,
    nome_status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_STATUS_EMPRESTIMO (
    cod_status INT AUTO_INCREMENT PRIMARY KEY,
    nome_status VARCHAR(50) NOT NULL
);

-- ==========================
-- Usuário e permissões
-- ==========================

CREATE TABLE IF NOT EXISTS TB_USUARIO (
    cod_username VARCHAR(255) PRIMARY KEY,
    txt_nome VARCHAR(100),
    cod_status INT,
    url_documento VARCHAR(255),
    dt_nascimento DATE,
    num_endereco VARCHAR(255),
    num_cep VARCHAR(10),
    cod_cpf VARCHAR(14) UNIQUE,
    num_telefone VARCHAR(20),
    senha_hash VARCHAR(255),
    txt_email VARCHAR(100),
    dt_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
    char_sexo CHAR(1),
    dt_desativacao DATETIME,
    dt_banimento DATETIME,
    num_max_slots INT DEFAULT 0,
    url_capa VARCHAR(255),
    FOREIGN KEY (cod_status) REFERENCES TB_STATUS_USUARIO(cod_status)
);

CREATE TABLE IF NOT EXISTS TB_USUARIO_ROLE (
    cod_username VARCHAR(255),
    cod_role INT,
    PRIMARY KEY (cod_username, cod_role),
    FOREIGN KEY (cod_username) REFERENCES TB_USUARIO(cod_username),
    FOREIGN KEY (cod_role) REFERENCES TB_ROLE(cod_role)
);

-- ==========================
-- Tabelas de obras e livros
-- ==========================

CREATE TABLE IF NOT EXISTS TB_EDITORA (
    cod_editora INT AUTO_INCREMENT PRIMARY KEY,
    nome_editora VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_OBRA (
    cod_obra INT AUTO_INCREMENT PRIMARY KEY,
    nome_obra VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_LIVRO (
    cod_livro INT AUTO_INCREMENT PRIMARY KEY,
    cod_editora INT,
    cod_obra INT,
    url_livro VARCHAR(255),
    txt_titulo VARCHAR(255),
    ano_lancamento YEAR,
    flag_ativo BOOLEAN DEFAULT TRUE,
    dt_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
    num_total_licencas INT DEFAULT 0,
    dt_validade DATE,
    uri_img_livro VARCHAR(255),
    bxt_sinopse TEXT,
    FOREIGN KEY (cod_editora) REFERENCES TB_EDITORA(cod_editora),
    FOREIGN KEY (cod_obra) REFERENCES TB_OBRA(cod_obra)
);

-- ==========================
-- Autores, temas e tags
-- ==========================

CREATE TABLE IF NOT EXISTS TB_AUTOR (
    cod_autor INT AUTO_INCREMENT PRIMARY KEY,
    nome_autor VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_LIVRO_AUTOR (
    cod_livro INT,
    cod_autor INT,
    PRIMARY KEY (cod_livro, cod_autor),
    FOREIGN KEY (cod_livro) REFERENCES TB_LIVRO(cod_livro),
    FOREIGN KEY (cod_autor) REFERENCES TB_AUTOR(cod_autor)
);

CREATE TABLE IF NOT EXISTS TB_TEMA (
    cod_tema INT AUTO_INCREMENT PRIMARY KEY,
    nome_tema VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_LIVRO_TEMA (
    cod_livro INT,
    cod_tema INT,
    PRIMARY KEY (cod_livro, cod_tema),
    FOREIGN KEY (cod_livro) REFERENCES TB_LIVRO(cod_livro),
    FOREIGN KEY (cod_tema) REFERENCES TB_TEMA(cod_tema)
);

CREATE TABLE IF NOT EXISTS TB_TAG (
    cod_tag INT AUTO_INCREMENT PRIMARY KEY,
    nome_tag VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_LIVRO_TAGS (
    cod_livro INT,
    cod_tag INT,
    PRIMARY KEY (cod_livro, cod_tag),
    FOREIGN KEY (cod_livro) REFERENCES TB_LIVRO(cod_livro),
    FOREIGN KEY (cod_tag) REFERENCES TB_TAG(cod_tag)
);

-- ==========================
-- Reservas e Empréstimos
-- ==========================

CREATE TABLE IF NOT EXISTS TB_RESERVA (
    cod_reserva INT AUTO_INCREMENT PRIMARY KEY,
    cod_username VARCHAR(255),
    cod_livro INT,
    dt_inicio_reserva DATETIME,
    dt_fim_reserva DATETIME,
    cod_status_reserva INT,
    FOREIGN KEY (cod_username) REFERENCES TB_USUARIO(cod_username),
    FOREIGN KEY (cod_livro) REFERENCES TB_LIVRO(cod_livro),
    FOREIGN KEY (cod_status_reserva) REFERENCES TB_STATUS_RESERVA(cod_status)
);

CREATE TABLE IF NOT EXISTS TB_EMPRESTIMO (
    cod_emprestimo INT AUTO_INCREMENT PRIMARY KEY,
    cod_livro INT,
    cod_username VARCHAR(255),
    cod_status INT,
    dt_inicio DATETIME,
    dt_fim DATETIME,
    num_renovacoes INT DEFAULT 0,
    num_pagina_atual INT DEFAULT 0,
    FOREIGN KEY (cod_livro) REFERENCES TB_LIVRO(cod_livro),
    FOREIGN KEY (cod_username) REFERENCES TB_USUARIO(cod_username),
    FOREIGN KEY (cod_status) REFERENCES TB_STATUS_EMPRESTIMO(cod_status)
);