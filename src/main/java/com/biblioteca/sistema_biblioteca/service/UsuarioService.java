package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository repository) {
        this.usuarioRepository = repository;
    }

    // CRUD
    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscaPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public void deletarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    // Métodos de Domínio

    public Emprestimo emprestarLivro(Long usuarioId, Livro livro) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Emprestimo emprestimo = usuario.emprestarLivro(livro);
        usuarioRepository.save(usuario);
        return emprestimo;
    }

    public void devolverLivro(Long usuarioId, Emprestimo emprestimo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuario.devolverLivro(emprestimo);
        usuarioRepository.save(usuario);
    }

    public Reserva reservarLivro(Long usuarioId, Livro livro) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Reserva reserva = usuario.reservarLivro(livro);
        usuarioRepository.save(usuario);
        return reserva;
    }

    public void cancelarReserva(Long usuarioId, Livro livro) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuario.cancelarReserva(livro);
        usuarioRepository.save(usuario);
    }

    public List<Emprestimo> consultaHistorico(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return usuario.consultaHistorico();
    }
}
