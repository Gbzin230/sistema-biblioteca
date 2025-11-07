-- Usuários de teste
INSERT INTO pessoa (id, role, username, nome, email, senha, sexo, flag_ativo)
VALUES (1, 'USUARIO', 'usuario_teste', 'Usuário Teste', 'usuario@teste.com', '123', 'M', true);

INSERT INTO pessoa (id, role, username, nome, email, senha, sexo, flag_ativo)
VALUES (2, 'USUARIO', 'maria_silva', 'Maria Silva', 'maria@teste.com', '123', 'F', true);

-- Livros de teste
INSERT INTO livro (id, titulo, autor, status, flag_ativo)
VALUES (1, 'Dom Casmurro', 'Machado de Assis', 'DISPONIVEL', true);

INSERT INTO livro (id, titulo, autor, status, flag_ativo)
VALUES (2, 'O Hobbit', 'J.R.R. Tolkien', 'DISPONIVEL', true);

INSERT INTO livro (id, titulo, autor, status, flag_ativo)
VALUES (3, '1984', 'George Orwell', 'DISPONIVEL', true);

