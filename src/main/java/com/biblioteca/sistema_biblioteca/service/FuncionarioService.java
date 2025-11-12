package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import org.springframework.stereotype.Service;

import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.FuncionarioRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, LivroRepository livroRepository,
            UsuarioRepository usuarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // CRUD
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

    // Métodos Administrativos

    // Livros
    public Livro cadastrarLivro(Livro livro) {
        livro.setFlagAtivo(true);
        livro.setStatus(Livro.Status.DISPONIVEL);
        return livroRepository.save(livro);
    }

    public void inativarLivro(Livro livro) {
        livro.setFlagAtivo(false);
        livro.setStatus(Livro.Status.INATIVO);
        livroRepository.save(livro);
    }

    public void ativarLivro(Livro livro) {
        livro.setFlagAtivo(true);
        livro.setStatus(Livro.Status.DISPONIVEL);
        livroRepository.save(livro);
    }

    public List<Livro> consultarLivros() {
        return livroRepository.findAll();
    }

    // Usuarios

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
