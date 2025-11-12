-- Usuários de teste
INSERT INTO pessoa (id, role, username, nome, email, senha, sexo, flag_ativo, limite_slots)
VALUES (1, 'USUARIO', 'usuario_teste', 'Usuário Teste', 'usuario@teste.com', '123', 'M', true, 3);

INSERT INTO pessoa (id, role, username, nome, email, senha, sexo, flag_ativo, limite_slots)
VALUES (2, 'USUARIO', 'maria_silva', 'Maria Silva', 'maria@teste.com', '123', 'F', true, 3);

-- Funcionário de teste
INSERT INTO pessoa (id, role, username, nome, email, senha, sexo, flag_ativo)
VALUES (3, 'FUNCIONARIO', 'funcionario_teste', 'Carlos Souza', 'funcionario@teste.com', '123456', 'M', true);

-- Admin de teste
INSERT INTO pessoa (id, role, username, nome, email, senha, sexo, flag_ativo)
VALUES (4, 'ADMIN', 'admin_teste', 'Ana Pereira', 'admin@teste.com', '123456', 'F', true);

-- Ajuste para que o autoincremento continue do próximo valor
ALTER TABLE pessoa ALTER COLUMN id RESTART WITH 5;

-- Livros de teste
INSERT INTO livro (id, titulo, autor, status, flag_ativo)
VALUES (1, 'Dom Casmurro', 'Machado de Assis', 'DISPONIVEL', true);
INSERT INTO livro (id, titulo, autor, status, flag_ativo)
VALUES (2, 'O Hobbit', 'J.R.R. Tolkien', 'DISPONIVEL', true);
INSERT INTO livro (id, titulo, autor, status, flag_ativo)
VALUES (3, '1984', 'George Orwell', 'DISPONIVEL', true);

-- Ajuste para que o autoincremento continue do próximo valor
ALTER TABLE livro ALTER COLUMN id RESTART WITH 4;


