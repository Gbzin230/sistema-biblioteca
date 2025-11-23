package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final StatusLivroRepository statusLivroRepository;
    private final StatusEmprestimoRepository statusEmprestimoRepository;
    private final StatusReservaRepository statusReservaRepository;

    private static final int PRAZO_PADRAO_DIAS = 7;
    private static final int MAX_RENOVACOES = 2;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository,
            StatusLivroRepository statusLivroRepository,
            StatusEmprestimoRepository statusEmprestimoRepository,
            StatusReservaRepository statusReservaRepository) {

        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.statusLivroRepository = statusLivroRepository;
        this.statusEmprestimoRepository = statusEmprestimoRepository;
        this.statusReservaRepository = statusReservaRepository;
    }

    // ====================================================================
    // REALIZAR EMPRÉSTIMO
    // ====================================================================
    @Transactional
    public Emprestimo realizarEmprestimo(String usuarioId, Long livroId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));

        StatusEmprestimo statusAtivo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVA não encontrado."));

        int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(usuario, statusAtivo);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, ativa);

        if (emprestimosAtivos + reservasAtivas >= 3) {
            throw new RegraNegocioException("Usuário atingiu o limite máximo de 3 slots.");
        }

        if (!usuario.getFlagAtivo()) {
            throw new RegraNegocioException("Usuário bloqueado não pode realizar empréstimos.");
        }

        StatusLivro emprestado = statusLivroRepository.findByNomeIgnoreCase("EMPRESTADO")
                .orElseThrow(() -> new RegraNegocioException("Status EMPRESTADO não existe."));

        StatusLivro reservado = statusLivroRepository.findByNomeIgnoreCase("RESERVADO")
                .orElseThrow(() -> new RegraNegocioException("Status RESERVADO não existe."));

        boolean livroReservado = livro.getStatus().getId().equals(reservado.getId());

        if (!livro.isDisponivel() && !livroReservado) {
            throw new RegraNegocioException("Livro não está disponível.");
        }

        Emprestimo emprestimo = new Emprestimo(usuario, livro);
        emprestimo.setDtInicio(LocalDateTime.now());
        emprestimo.setDtFim(LocalDateTime.now().plusDays(PRAZO_PADRAO_DIAS));
        emprestimo.setStatus(statusAtivo);

        livro.setStatus(emprestado);
        livroRepository.save(livro);

        return emprestimoRepository.save(emprestimo);
    }

    // ====================================================================
    // RENOVAR EMPRÉSTIMO
    // ====================================================================
    @Transactional
    public Emprestimo renovarEmprestimo(Long emprestimoId) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));

        if (!emprestimo.getStatus().equals(ativo)) {
            throw new RegraNegocioException("Apenas empréstimos ativos podem ser renovados.");
        }

        if (emprestimo.getNumRenovacoes() >= MAX_RENOVACOES) {
            throw new RegraNegocioException("Máximo de renovações atingido.");
        }

        emprestimo.setNumRenovacoes(emprestimo.getNumRenovacoes() + 1);
        emprestimo.setDtFim(emprestimo.getDtFim().plusDays(PRAZO_PADRAO_DIAS));

        return emprestimoRepository.save(emprestimo);
    }

    // ====================================================================
    // DEVOLVER LIVRO
    // ====================================================================
    @Transactional
    public void devolverLivro(Long emprestimoId) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));

        StatusEmprestimo finalizado = statusEmprestimoRepository.findByNomeIgnoreCase("FINALIZADO")
                .orElseThrow(() -> new RegraNegocioException("Status FINALIZADO não encontrado."));

        emprestimo.setStatus(finalizado);
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();
        if (livro == null) return;

        StatusLivro disponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status DISPONIVEL não existe."));

        StatusLivro emprestado = statusLivroRepository.findByNomeIgnoreCase("EMPRESTADO")
                .orElseThrow(() -> new RegraNegocioException("Status EMPRESTADO não existe."));

        StatusLivro reservado = statusLivroRepository.findByNomeIgnoreCase("RESERVADO")
                .orElseThrow(() -> new RegraNegocioException("Status RESERVADO não existe."));

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVA não existe."));

        StatusReserva confirmada = statusReservaRepository.findByNomeIgnoreCase("CONFIRMADA")
                .orElseThrow(() -> new RegraNegocioException("Status CONFIRMADA não existe."));

        List<Reserva> fila = reservaRepository
                .findByLivroAndStatusOrderByDtInicioReservaAsc(livro, ativa);

        boolean emprestadoParaReserva = false;

        for (Reserva reserva : fila) {

            Usuario user = reserva.getUsuario();

            int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(user, ativo);
            int reservasAtivas = reservaRepository.countByUsuarioAndStatus(user, ativa);

            if (user.getFlagAtivo() && emprestimosAtivos + reservasAtivas < 3) {

                Emprestimo novoEmprestimo = new Emprestimo(user, livro);
                novoEmprestimo.setDtInicio(LocalDateTime.now());
                novoEmprestimo.setDtFim(LocalDateTime.now().plusDays(PRAZO_PADRAO_DIAS));
                novoEmprestimo.setStatus(ativo);

                livro.setStatus(emprestado);
                emprestimoRepository.save(novoEmprestimo);

                reserva.setStatus(confirmada);
                reservaRepository.save(reserva);

                emprestadoParaReserva = true;
                break;
            }
        }

        if (!emprestadoParaReserva) {
            livro.setStatus(fila.isEmpty() ? disponivel : reservado);
        }

        livroRepository.save(livro);
    }

    // ====================================================================
    // CONSULTAS
    // ====================================================================
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

        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));

        LocalDateTime agora = LocalDateTime.now();

        return emprestimoRepository.findAll().stream()
                .filter(e -> e.getStatus().equals(ativo))
                .filter(e -> e.getDtFim().isBefore(agora))
                .collect(Collectors.toList());
    }

    public int countEmprestimosAtivos(Usuario usuario) {
        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));
        return emprestimoRepository.countByUsuarioAndStatus(usuario, ativo);
    }

    // ====================================================================
    // SEGURANÇA
    // ====================================================================
    public void validarDonoDoEmprestimo(Long emprestimoId, String username) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        if (!emprestimo.getUsuario().getUsername().equals(username)) {
            throw new AccessDeniedException("Você não pode acessar empréstimos de outro usuário.");
        }
    }

    public void validarUsuarioEmprestimo(String usuarioId, String username) {

        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        if (!usuario.getUsername().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode realizar empréstimos em seu próprio nome.");
        }
    }

    public void devolverAutorizado(Long emprestimoId, String username) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = usuario.getRole().getNome();

        if (role.equalsIgnoreCase("USUARIO") &&
                !emprestimo.getUsuario().getUsername().equals(username)) {

            throw new AccessDeniedException("Você não pode devolver empréstimos de outro usuário.");
        }

        devolverLivro(emprestimoId);
    }
}
