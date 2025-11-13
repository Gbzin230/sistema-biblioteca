USE biblioteca_db;

INSERT IGNORE INTO TB_STATUS_EMPRESTIMO (cod_status, nome_status)
VALUES (1,'ATIVO'),(2,'FINALIZADO'),(3,'ATRASADO');

INSERT IGNORE INTO TB_STATUS_RESERVA (cod_status, nome_status)
VALUES (1,'ATIVA'),(2,'CONFIRMADA'),(3,'CANCELADA');

INSERT IGNORE INTO TB_USUARIO (cod_username, txt_nome, dt_nascimento, num_endereco, num_cep, cod_cpf, num_telefone, senha_hash, txt_email, char_sexo, role, flag_ativo, limite_slots)
VALUES
('admin','Administrador','1980-01-01','Endereco Admin','01001000','00000000000','(11)90000-0000','Admin@123','admin@local','M','ADMIN', TRUE, 3),
('funcionario','Funcionario','1985-01-01','Endereco Funcionario','01002000','00000000001','(11)90000-0001','Func@123','func@local','M','FUNCIONARIO', TRUE, 3),
('usuario','Usuario Um','1990-01-01','Endereco U1','01003000','00000000002','(11)90000-0002','User@123','u1@local','M','USUARIO', TRUE, 3),
('usuario2','Usuario Dois','1991-02-02','Endereco U2','01004000','00000000003','(11)90000-0003','User@123','u2@local','F','USUARIO', TRUE, 3);

INSERT IGNORE INTO TB_LIVRO (txt_titulo, num_ano_lancamento, flag_ativo, status, txt_sinopse)
VALUES
('Dom Casmurro',1899,TRUE,'DISPONIVEL','Romance clássico'),
('O Hobbit',1937,TRUE,'DISPONIVEL','Fantasia'),
('1984',1949,TRUE,'DISPONIVEL','Distopia'),
('Grande Sertão: Veredas',1956,TRUE,'DISPONIVEL','Romance brasileiro');