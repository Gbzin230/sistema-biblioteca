package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final EmprestimoService emprestimoService;
    private final PessoaRepository pessoaRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          LivroRepository livroRepository,
                          EmprestimoService emprestimoService,
                          PessoaRepository pessoaRepository) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.emprestimoService = emprestimoService;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public Reserva criarReserva(Reserva reserva) {
        if (reserva.getUsuario() == null || reserva.getLivro() == null) {
            throw new RegraNegocioException("Reserva deve conter usuário e livro.");
        }

        Usuario usuario = reserva.getUsuario();

        // 🔹 Verifica o limite de slots (empréstimos + reservas)
        int emprestimosAtivos = emprestimoService.countEmprestimosAtivos(usuario);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, Reserva.ReservaStatus.ATIVA);
        int totalSlots = emprestimosAtivos + reservasAtivas;

        if (totalSlots >= 3) {
            throw new RegraNegocioException("Usuário atingiu o limite máximo de 3 slots (empréstimos + reservas).");
        }

        Livro livro = reserva.getLivro();

        if (livro.isDisponivel()) {
            emprestimoService.realizarEmprestimo(usuario.getId(), livro.getId());
            reserva.setStatus(Reserva.ReservaStatus.CONFIRMADA);
            return reservaRepository.save(reserva);
        }

        reserva.setStatus(Reserva.ReservaStatus.ATIVA);
        Reserva salva = reservaRepository.save(reserva);
        if (livro.getStatus() != Livro.Status.EMPRESTADO) {
            livro.alterarStatus(Livro.Status.RESERVADO);
            livroRepository.save(livro);
        }
        return salva;
    }


    @Transactional
    public void confirmarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Livro livro = reserva.getLivro();
        if (!livro.isDisponivel() && livro.getStatus() != Livro.Status.RESERVADO) {
            throw new RegraNegocioException("Livro não disponível para empréstimo.");
        }

        emprestimoService.realizarEmprestimo(reserva.getUsuario().getId(), livro.getId());
        reserva.setStatus(Reserva.ReservaStatus.CONFIRMADA);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada"));

        reserva.cancelar();
        reservaRepository.save(reserva);

        Livro livro = reserva.getLivro();
        boolean aindaReservado = reservaRepository.existsByLivroAndStatus(livro, Reserva.ReservaStatus.ATIVA);

        if (!aindaReservado && livro.getStatus() == Livro.Status.RESERVADO) {
            livro.alterarStatus(Livro.Status.DISPONIVEL);
            livroRepository.save(livro);
        }
    }

    @Transactional(readOnly = true)
    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    // 🔐 Valida se o usuário logado é o mesmo que está criando a reserva
    public void validarUsuarioReserva(Long usuarioId, String username) {
        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        if (!pessoa.getId().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode criar reservas em seu próprio nome.");
        }
    }

    // 🔐 Valida se o usuário pode cancelar a reserva (dono ou admin)
    public void cancelarAutorizado(Long reservaId, String username) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = pessoa.getRoleString();

        if (role.equalsIgnoreCase("USUARIO") &&
                !reserva.getUsuario().getUsername().equals(username)) {
            throw new AccessDeniedException("Você não pode cancelar reservas de outro usuário.");
        }

        cancelarReserva(reservaId); // chama o método original
    }

}


