package com.biblioteca.sistema_biblioteca.service;

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

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             UsuarioRepository usuarioRepository,
                             LivroRepository livroRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
    }

    @Transactional
    public Emprestimo criarEmprestimo(Long usuarioId, Long livroId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        if (!"DISPONIVEL".equals(livro.getStatus())) {
            throw new RuntimeException("Livro não está disponível para empréstimo");
        }

        // Verificar slots
        long emprestimosAtivos = emprestimoRepository.findByUsuarioAndStatus(usuario, "ATIVO").size();
        if (emprestimosAtivos >= usuario.getLimiteSlots()) {
            throw new RuntimeException("Usuário atingiu o limite de empréstimos");
        }

        livro.setStatus("ALUGADO");

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDtInicio(LocalDate.now());
        emprestimo.setDtPrevistaDevolucao(LocalDate.now().plusDays(14));
        emprestimo.setStatus("ATIVO");
        emprestimo.setNumRenovacoes(0);

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo renovar(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));

        if (!emprestimo.renovar()) {
            throw new RuntimeException("Não foi possível renovar o empréstimo");
        }

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public void encerrar(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));

        emprestimo.encerrar();
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();
        livro.setStatus("DISPONIVEL");
        livroRepository.save(livro);
    }
}
