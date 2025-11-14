package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
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

    public ReservaService(
            ReservaRepository reservaRepository,
            LivroRepository livroRepository,
            EmprestimoService emprestimoService,
            PessoaRepository pessoaRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.emprestimoService = emprestimoService;
        this.pessoaRepository = pessoaRepository;
    }

    // ==========================================================
    // CRIAR RESERVA
    // ==========================================================

    @Transactional
    public Reserva criarReserva(Reserva reserva) {

        if (reserva.getUsuario() == null || reserva.getLivro() == null) {
            throw new RegraNegocioException("Reserva deve conter usuário e livro.");
        }

        Usuario usuario = reserva.getUsuario();

        // 🔹 contador de slots ativos
        int emprestimosAtivos = emprestimoService.countEmprestimosAtivos(usuario);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, Reserva.ReservaStatus.ATIVA);
        int totalSlots = emprestimosAtivos + reservasAtivas;

        if (totalSlots >= 3) {
            throw new RegraNegocioException("Usuário atingiu o limite máximo de 3 slots (empréstimos + reservas).");
        }

        Livro livro = reserva.getLivro();

        // 🔹 Livro disponível → vira empréstimo imediatamente
        if (livro.isDisponivel()) {
            emprestimoService.realizarEmprestimo(usuario.getUsername(), livro.getId()); // ID agora é String
            reserva.setStatus(Reserva.ReservaStatus.CONFIRMADA);
            return reservaRepository.save(reserva);
        }

        // 🔹 Caso contrário → vira reserva ativa
        reserva.setStatus(Reserva.ReservaStatus.ATIVA);
        Reserva salva = reservaRepository.save(reserva);

        if (livro.getStatus() != Livro.Status.EMPRESTADO) {
            livro.alterarStatus(Livro.Status.RESERVADO);
            livroRepository.save(livro);
        }

        return salva;
    }

    // ==========================================================
    // CONFIRMAR RESERVA → vira EMPRÉSTIMO
    // ==========================================================

    @Transactional
    public void confirmarReserva(Long reservaId) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Livro livro = reserva.getLivro();

        if (!livro.isDisponivel() && livro.getStatus() != Livro.Status.RESERVADO) {
            throw new RegraNegocioException("Livro não disponível para empréstimo.");
        }

        emprestimoService.realizarEmprestimo(
                reserva.getUsuario().getUsername(),   // ✔ agora String
                livro.getId()
        );

        reserva.setStatus(Reserva.ReservaStatus.CONFIRMADA);
        reservaRepository.save(reserva);
    }

    // ==========================================================
    // CANCELAR RESERVA
    // ==========================================================

    @Transactional
    public void cancelarReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada"));

        reserva.cancelar();
        reservaRepository.save(reserva);

        Livro livro = reserva.getLivro();

        boolean aindaReservado = reservaRepository.existsByLivroAndStatus(
                livro, Reserva.ReservaStatus.ATIVA
        );

        // Se ninguém mais estiver reservando → livro volta a estar disponível
        if (!aindaReservado && livro.getStatus() == Livro.Status.RESERVADO) {
            livro.alterarStatus(Livro.Status.DISPONIVEL);
            livroRepository.save(livro);
        }
    }

    // ==========================================================
    // LISTAR
    // ==========================================================

    @Transactional(readOnly = true)
    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    // ==========================================================
    // VALIDAÇÃO: usuário logado só pode reservar para ele mesmo
    // ==========================================================

    public void validarUsuarioReserva(String usuarioId, String usernameLogado) {

        Pessoa pessoa = pessoaRepository.findByUsername(usernameLogado)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        if (!pessoa.getUsername().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode criar reservas em seu próprio nome.");
        }
    }

    // ==========================================================
    // CANCELAMENTO SEGURO
    // ==========================================================

    public void cancelarAutorizado(Long reservaId, String usernameLogado) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Pessoa pessoa = pessoaRepository.findByUsername(usernameLogado)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = pessoa.getRoleString();

        // Usuário comum só cancela a própria reserva
        if (role.equalsIgnoreCase("USUARIO") &&
                !reserva.getUsuario().getUsername().equals(usernameLogado)) {
            throw new AccessDeniedException("Você não pode cancelar reservas de outro usuário.");
        }

        cancelarReserva(reservaId);
    }
}
