    package com.biblioteca.sistema_biblioteca.service;

    import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
    import com.biblioteca.sistema_biblioteca.model.Emprestimo;
    import com.biblioteca.sistema_biblioteca.model.Livro;
    import com.biblioteca.sistema_biblioteca.model.Pessoa;
    import com.biblioteca.sistema_biblioteca.model.Usuario;
    import com.biblioteca.sistema_biblioteca.repository.EmprestimoRepository;
    import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
    import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
    import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import java.time.LocalDate;
    import java.util.List;

    @Service
    public class EmprestimoService {

        private final EmprestimoRepository emprestimoRepository;
        private final LivroRepository livroRepository;
        private final UsuarioRepository usuarioRepository;

        public EmprestimoService(EmprestimoRepository emprestimoRepository,
                                 LivroRepository livroRepository,
                                 UsuarioRepository usuarioRepository) {
            this.emprestimoRepository = emprestimoRepository;
            this.livroRepository = livroRepository;
            this.usuarioRepository = usuarioRepository;
        }

        @Transactional
        public Emprestimo realizarEmprestimo(Long pessoaId, Long livroId) {
            Usuario usuario = usuarioRepository.findById(pessoaId)
                    .orElseThrow(() -> new RegraNegocioException("Pessoa não encontrada."));

            Livro livro = livroRepository.findById(livroId)
                    .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));

            // Bloqueio de usuário
            if (!usuario.isFlagAtivo()) {
                throw new RegraNegocioException("Usuário bloqueado não pode realizar empréstimos.");
            }

            // Disponibilidade do livro
            if (!livro.isDisponivel()) {
                throw new RegraNegocioException("Livro não disponível para empréstimo.");
            }

            Emprestimo emprestimo = new Emprestimo();
            emprestimo.setUsuario(usuario);
            emprestimo.setLivro(livro);
            emprestimo.setDtInicio(LocalDate.now());
            emprestimo.setDtPrevistaDevolucao(LocalDate.now().plusDays(7));

            livro.setFlagAtivo(false);
            livroRepository.save(livro);

            return emprestimoRepository.save(emprestimo);
        }

        @Transactional
        public Emprestimo renovarEmprestimo(Long emprestimoId) {
            Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                    .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

            // 🔹 Verifica se o empréstimo ainda está ativo
            if (!"ATIVO".equals(emprestimo.getStatus())) {
                throw new RegraNegocioException("Empréstimo já foi encerrado ou está atrasado.");
            }

            // 🔹 Verifica limite de renovações (máx. 2)
            if (emprestimo.getNumRenovacoes() != null && emprestimo.getNumRenovacoes() >= 2) {
                throw new RegraNegocioException("Limite máximo de renovações atingido.");
            }

            // 🔹 Realiza a renovação (14 dias extras por padrão)
            emprestimo.setDtPrevistaDevolucao(emprestimo.getDtPrevistaDevolucao().plusDays(14));
            emprestimo.setNumRenovacoes(emprestimo.getNumRenovacoes() + 1);

            return emprestimoRepository.save(emprestimo);
        }


        @Transactional
        public void devolverLivro(Long emprestimoId) {
            Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                    .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

            if (emprestimo.getDtInicio() != null) {
                throw new RegraNegocioException("Este empréstimo já foi finalizado.");
            }

            emprestimo.setDtInicio(LocalDate.now());
            emprestimo.getLivro().setFlagAtivo(true);
            livroRepository.save(emprestimo.getLivro());
            emprestimoRepository.save(emprestimo);
        }

        public List<Emprestimo> listarEmprestimos() {
            return emprestimoRepository.findAll();
        }
    }

