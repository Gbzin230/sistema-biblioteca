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
    private final UsuarioRepository usuarioRepository;
    private final StatusLivroRepository statusLivroRepository;
    private final StatusReservaRepository statusReservaRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            LivroRepository livroRepository,
            EmprestimoService emprestimoService,
            UsuarioRepository usuarioRepository,
            StatusLivroRepository statusLivroRepository,
            StatusReservaRepository statusReservaRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.emprestimoService = emprestimoService;
        this.usuarioRepository = usuarioRepository;
        this.statusLivroRepository = statusLivroRepository;
        this.statusReservaRepository = statusReservaRepository;
    }

    // ==========================================================
    // CRIAR RESERVA
    // ==========================================================

    @Transactional
    public Reserva criarReserva(Reserva reserva) {

        if (reserva.getUsuario() == null || reserva.getLivro() == null) {
            throw new RegraNegocioException("Reserva deve conter usuário e livro.");
        }

        Usuario usuario = usuarioRepository.findById(reserva.getUsuario().getUsername())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        Livro livro = reserva.getLivro();

        // -------- CONTAGEM DE SLOTS --------
        int emprestimosAtivos = emprestimoService.countEmprestimosAtivos(usuario);

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status 'ATIVA' não existe."));

        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, ativa);

        if (emprestimosAtivos + reservasAtivas >= 3) {
            throw new RegraNegocioException("Usuário atingiu o limite máximo de 3 slots.");
        }

        // -------- LIVRO DISPONÍVEL? --------
        boolean disponivel =
                livro.getStatus().getNome().equalsIgnoreCase("DISPONIVEL")
                        && Boolean.TRUE.equals(livro.getFlagAtivo());

        if (disponivel) {

            emprestimoService.realizarEmprestimo(
                    usuario.getUsername(),
                    livro.getId()
            );

            StatusReserva confirmada = statusReservaRepository.findByNomeIgnoreCase("CONFIRMADA")
                    .orElseThrow(() -> new RegraNegocioException("Status 'CONFIRMADA' não existe."));

            reserva.setStatus(confirmada);

            return reservaRepository.save(reserva);
        }

        // -------- RESERVA NORMAL (ATIVA) --------
        reserva.setStatus(ativa);

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

        StatusReserva confirmada = statusReservaRepository.findByNomeIgnoreCase("CONFIRMADA")
                .orElseThrow(() -> new RegraNegocioException("Status 'CONFIRMADA' não existe."));

        reserva.setStatus(confirmada);
        reservaRepository.save(reserva);
    }

    // ==========================================================
    // CANCELAR RESERVA
    // ==========================================================

    @Transactional
    public void cancelarReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        StatusReserva cancelada = statusReservaRepository.findByNomeIgnoreCase("CANCELADA")
                .orElseThrow(() -> new RegraNegocioException("Status 'CANCELADA' não existe."));

        reserva.setStatus(cancelada);
        reservaRepository.save(reserva);

        Livro livro = reserva.getLivro();

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status 'ATIVA' não existe."));

        boolean aindaReservado = reservaRepository.existsByLivroAndStatus(livro, ativa);

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

        // ============================================================
        // 📚 HISTÓRICO DE RESERVAS COM REGRAS DE SEGURANÇA
        // ============================================================
        @Transactional(readOnly = true)
        public List<Reserva> buscarHistoricoReservas(String usernameConsulta, String usernameAuth) {

        Usuario authUser = usuarioRepository.findById(usernameAuth)
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));

        Usuario alvo = usuarioRepository.findById(usernameConsulta)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = authUser.getRole().getNome();

        // 🔒 Usuário comum só pode consultar o próprio histórico
        if (role.equalsIgnoreCase("USUARIO") &&
                !authUser.getUsername().equals(alvo.getUsername())) {
                throw new AccessDeniedException("Você não pode consultar o histórico de reservas de outro usuário.");
        }

        return reservaRepository.findByUsuarioOrderByDtInicioReservaDesc(alvo);
        }


    // ==========================================================
    // VALIDAÇÃO DE SEGURANÇA
    // ==========================================================

    public void validarUsuarioReserva(String usuarioId, String usernameLogado) {

        Usuario usuarioLogado = usuarioRepository.findById(usernameLogado)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        if (!usuarioLogado.getUsername().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode criar reservas em seu próprio nome.");
        }
    }

    // ==========================================================
    // CANCELAMENTO AUTORIZADO
    // ==========================================================

    public void cancelarAutorizado(Long reservaId, String usernameLogado) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Usuario usuarioLogado = usuarioRepository.findById(usernameLogado)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String roleName = usuarioLogado.getRole().getNome();

        if ("USUARIO".equalsIgnoreCase(roleName) &&
                !reserva.getUsuario().getUsername().equals(usernameLogado)) {

            throw new AccessDeniedException("Você não pode cancelar reservas de outro usuário.");
        }

        cancelarReserva(reservaId);
    }
}
