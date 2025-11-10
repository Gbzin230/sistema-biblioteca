package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.EmprestimoRepository;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EmprestimoRepository emprestimoRepository,
                          ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
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

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        boolean temEmprestimo = emprestimoRepository.existsByUsuario(usuario);
        boolean temReserva = reservaRepository.existsByUsuario(usuario);

        if (temEmprestimo || temReserva) {
            throw new RegraNegocioException(
                    "Não é possível deletar o usuário. Ele possui histórico de empréstimos ou reservas no sistema."
            );
        }

        // Garante que nada pendente é mandado pro banco antes do delete
        usuarioRepository.flush();

        usuarioRepository.delete(usuario);
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
