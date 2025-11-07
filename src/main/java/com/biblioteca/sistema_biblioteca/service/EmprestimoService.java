package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.EmprestimoRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    private static final int PRAZO_PADRAO_DIAS = 7;
    private static final int MAX_RENOVACOES = 2;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             LivroRepository livroRepository,
                             UsuarioRepository usuarioRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Emprestimo realizarEmprestimo(Long usuarioId, Long livroId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));

        // Bloqueio de usuário
        if (Boolean.FALSE.equals(usuario.isFlagAtivo())) {
            throw new RegraNegocioException("Usuário bloqueado não pode realizar empréstimos.");
        }

        // Limite de empréstimos do usuário (exemplo: 5)
        int limiteEmprestimos = 5;
        long emprestimosAtivosDoUsuario = emprestimoRepository.findAll().stream()
                .filter(e -> e.getUsuario() != null && Objects.equals(e.getUsuario().getId(), usuarioId))
                .filter(e -> "ATIVO".equals(e.getStatus()))
                .count();
        if (emprestimosAtivosDoUsuario >= limiteEmprestimos) {
            throw new RegraNegocioException("Usuário atingiu o limite de empréstimos ativos.");
        }

        // Disponibilidade do livro
        if (!livro.isDisponivel()) {
            throw new RegraNegocioException("Livro não disponível para empréstimo.");
        }

        // Criar empréstimo
        Emprestimo emprestimo = new Emprestimo(usuario, livro); // usa construtor do model (configura datas padrão)
        // Garantir dtPrevistaDevolucao correto (caso modelo não tenha setado)
        if (emprestimo.getDtPrevistaDevolucao() == null) {
            emprestimo.setDtPrevistaDevolucao(LocalDate.now().plusDays(PRAZO_PADRAO_DIAS));
        }

        // Atualizar status do livro
        livro.alterarStatus(Livro.Status.EMPRESTADO);
        livroRepository.save(livro);

        // Persistir empréstimo
        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo renovarEmprestimo(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        // Só renova se estiver ativo
        if (!"ATIVO".equals(emprestimo.getStatus())) {
            throw new RegraNegocioException("Apenas empréstimos ATIVOS podem ser renovados.");
        }

        if (emprestimo.getNumRenovacoes() == null) {
            emprestimo.setNumRenovacoes(0);
        }

        if (emprestimo.getNumRenovacoes() >= MAX_RENOVACOES) {
            throw new RegraNegocioException("Máximo de renovações atingido.");
        }

        // Executa a renovação (se modelo tem método renovar, usa; senão ajusta diretamente)
        boolean renovou = emprestimo.renovar();
        if (!renovou) {
            // fallback: aplicar manualmente
            emprestimo.setDtPrevistaDevolucao(emprestimo.getDtPrevistaDevolucao().plusDays(PRAZO_PADRAO_DIAS * 2));
            emprestimo.setNumRenovacoes(emprestimo.getNumRenovacoes() + 1);
        }

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public void devolverLivro(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        if (!"ATIVO".equals(emprestimo.getStatus())) {
            throw new RegraNegocioException("Este empréstimo já foi finalizado ou não está ativo.");
        }

        // Encerrar empréstimo
        emprestimo.encerrar();
        emprestimoRepository.save(emprestimo);

        // Liberar livro
        Livro livro = emprestimo.getLivro();
        if (livro != null) {
            livro.alterarStatus(Livro.Status.DISPONIVEL);
            livroRepository.save(livro);
        }
    }

    @Transactional(readOnly = true)
    public Emprestimo buscarPorId(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarEmprestimos() {
        return emprestimoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> buscarEmprestimosAtrasados() {
        LocalDate hoje = LocalDate.now();
        return emprestimoRepository.findAll().stream()
                .filter(e -> "ATIVO".equals(e.getStatus()))
                .filter(e -> e.getDtPrevistaDevolucao() != null && e.getDtPrevistaDevolucao().isBefore(hoje))
                .collect(Collectors.toList());
    }
}
