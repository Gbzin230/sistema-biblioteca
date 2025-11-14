package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.StatusLivro;
import com.biblioteca.sistema_biblioteca.model.Usuario;

import com.biblioteca.sistema_biblioteca.repository.FuncionarioRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.StatusLivroRepository;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final StatusLivroRepository statusLivroRepository;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            StatusLivroRepository statusLivroRepository
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.statusLivroRepository = statusLivroRepository;
    }

    // ============================================================
    // CRUD Funcionario
    // ============================================================
    public Funcionario salvarFuncionario(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }

    public List<Funcionario> listarFuncionarios() {
        return funcionarioRepository.findAll();
    }

    public Optional<Funcionario> buscarFuncionarioPorId(Long id) {
        return funcionarioRepository.findById(id);
    }

    public void deletarFuncionario(Long id) {
        funcionarioRepository.deleteById(id);
    }

    // ============================================================
    // LIVROS
    // ============================================================

    @Transactional
    public Livro cadastrarLivro(Livro livro) {

        livro.setFlagAtivo(true);

        StatusLivro statusDisponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status DISPONIVEL não encontrado."));

        livro.setStatus(statusDisponivel);

        return livroRepository.save(livro);
    }

    @Transactional
    public void inativarLivro(Livro livro) {

        livro.setFlagAtivo(false);

        StatusLivro statusIndisponivel = statusLivroRepository.findByNomeIgnoreCase("INDISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status INDISPONIVEL não encontrado."));

        livro.setStatus(statusIndisponivel);

        livroRepository.save(livro);
    }

    @Transactional
    public void ativarLivro(Livro livro) {

        livro.setFlagAtivo(true);

        StatusLivro statusDisponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status DISPONIVEL não encontrado."));

        livro.setStatus(statusDisponivel);

        livroRepository.save(livro);
    }

    public List<Livro> consultarLivros() {
        return livroRepository.findAll();
    }

    // ============================================================
    // USUÁRIOS
    // ============================================================

    public List<Usuario> consultarUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Livro> consultarHistoricoUsuario(Usuario usuario) {
        return usuario.consultaHistorico().stream()
                .map(Emprestimo::getLivro)
                .toList();
    }

    public boolean aprovarUsuario(Usuario usuario) {

        usuario.setFlagAtivo(true);
        usuarioRepository.save(usuario);
        return true;
    }

}
