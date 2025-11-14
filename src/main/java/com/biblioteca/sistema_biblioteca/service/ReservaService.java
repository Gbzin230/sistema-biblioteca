package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;

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
    private final StatusLivroRepository statusLivroRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            LivroRepository livroRepository,
            EmprestimoService emprestimoService,
            PessoaRepository pessoaRepository,
            StatusLivroRepository statusLivroRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.emprestimoService = emprestimoService;
        this.pessoaRepository = pessoaRepository;
        this.statusLivroRepository = statusLivroRepository;
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

        int emprestimosAtivos = emprestimoService.countEmprestimosAtivos(usuario);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, reserva.getStatus());
        int totalSlots = emprestimosAtivos + reservasAtivas;

        if (totalSlots >= 3) {
            throw new RegraNegocioException("Usuário atingiu o limite máximo de 3 slots.");
        }

        Livro livro = reserva.getLivro();

        boolean disponivel = livro.getStatus().getNome().equalsIgnoreCase("DISPONIVEL")
                && Boolean.TRUE.equals(livro.getFlagAtivo());

        if (disponivel) {

            emprestimoService.realizarEmprestimo(usuario.getUsername(), livro.getId());

            reserva.setStatus(reserva.getStatus()); // provavelmente CONFIRMADA pelo construtor
            return reservaRepository.save(reserva);
        }

        reserva.setStatus(reserva.getStatus()); // ATIVA
        Reserva salva = reservaRepository.save(reserva);

        boolean jaEmprestado = livro.getStatus().getNome().equalsIgnoreCase("EMPRESTADO");

        if (!jaEmprestado) {
            StatusLivro reservado = statusLivroRepository.findByNomeIgnoreCase("RESERVADO")
                    .orElseThrow(() -> new RegraNegocioException("Status 'RESERVADO' não existe."));
            livro.setStatus(reservado);
            livroRepository.save(livro);
        }

        return salva;
    }

    // ==========================================================
    // CONFIRMAR RESERVA
    // ==========================================================

    @Transactional
    public void confirmarReserva(Long reservaId) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Livro livro = reserva.getLivro();

        boolean disponivel = livro.getStatus().getNome().equalsIgnoreCase("DISPONIVEL");

        if (!disponivel) {
            throw new RegraNegocioException("Livro não disponível para empréstimo.");
        }

        emprestimoService.realizarEmprestimo(
                reserva.getUsuario().getUsername(),
                livro.getId()
        );

        reserva.setStatus(reserva.getStatus()); // CONFIRMADA
        reservaRepository.save(reserva);
    }

    // ==========================================================
    // CANCELAR RESERVA
    // ==========================================================

    @Transactional
    public void cancelarReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        reserva.cancelar();
        reservaRepository.save(reserva);

        Livro livro = reserva.getLivro();

        boolean aindaReservado = reservaRepository.existsByLivroAndStatus(
                livro, reserva.getStatus());

        if (!aindaReservado) {
            StatusLivro disponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                    .orElseThrow(() -> new RegraNegocioException("Status 'DISPONIVEL' não existe."));

            livro.setStatus(disponivel);
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
    // VALIDAÇÃO DE SEGURANÇA
    // ==========================================================

    public void validarUsuarioReserva(String usuarioId, String usernameLogado) {

        Pessoa pessoa = pessoaRepository.findByUsername(usernameLogado)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        if (!pessoa.getUsername().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode criar reservas em seu próprio nome.");
        }
    }

    // ==========================================================
    // CANCELAMENTO AUTORIZADO
    // ==========================================================

    public void cancelarAutorizado(Long reservaId, String usernameLogado) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Pessoa pessoa = pessoaRepository.findByUsername(usernameLogado)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = pessoa.getRoleString();

        if (role.equalsIgnoreCase("USUARIO") &&
                !reserva.getUsuario().getUsername().equals(usernameLogado)) {

            throw new AccessDeniedException("Você não pode cancelar reservas de outro usuário.");
        }

        cancelarReserva(reservaId);
    }
}
