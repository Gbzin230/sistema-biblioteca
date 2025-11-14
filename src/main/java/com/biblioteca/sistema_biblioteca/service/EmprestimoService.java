package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final PessoaRepository pessoaRepository;
    private final StatusLivroRepository statusLivroRepository;

    private static final int PRAZO_PADRAO_DIAS = 7;
    private static final int MAX_RENOVACOES = 2;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             LivroRepository livroRepository,
                             UsuarioRepository usuarioRepository,
                             ReservaRepository reservaRepository,
                             PessoaRepository pessoaRepository,
                             StatusLivroRepository statusLivroRepository) {

        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.pessoaRepository = pessoaRepository;
        this.statusLivroRepository = statusLivroRepository;
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

        int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(usuario, Emprestimo.Status.ATIVO);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, Reserva.ReservaStatus.ATIVA);

        if (emprestimosAtivos + reservasAtivas >= 3) {
            throw new RegraNegocioException("Usuário atingiu o limite máximo de 3 slots.");
        }

        if (!usuario.isFlagAtivo()) {
            throw new RegraNegocioException("Usuário bloqueado não pode realizar empréstimos.");
        }

        StatusLivro reservado = statusLivroRepository.findByNomeIgnoreCase("RESERVADO")
                .orElseThrow(() -> new RegraNegocioException("Status 'RESERVADO' não existe."));

        StatusLivro emprestado = statusLivroRepository.findByNomeIgnoreCase("EMPRESTADO")
                .orElseThrow(() -> new RegraNegocioException("Status 'EMPRESTADO' não existe."));

        if (!livro.isDisponivel() && livro.getStatus() != reservado) {
            throw new RegraNegocioException("Livro não está disponível.");
        }

        Emprestimo emprestimo = new Emprestimo(usuario, livro);
        emprestimo.setDtInicio(LocalDate.now());
        emprestimo.setDtPrevistaDevolucao(LocalDate.now().plusDays(PRAZO_PADRAO_DIAS));
        emprestimo.setStatus(Emprestimo.Status.ATIVO);

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

        if (emprestimo.getStatus() != Emprestimo.Status.ATIVO) {
            throw new RegraNegocioException("Apenas empréstimos ativos podem ser renovados.");
        }

        if (emprestimo.getNumRenovacoes() == null) {
            emprestimo.setNumRenovacoes(0);
        }

        if (emprestimo.getNumRenovacoes() >= MAX_RENOVACOES) {
            throw new RegraNegocioException("Máximo de renovações atingido.");
        }

        emprestimo.setNumRenovacoes(emprestimo.getNumRenovacoes() + 1);
        emprestimo.setDtPrevistaDevolucao(
                emprestimo.getDtPrevistaDevolucao().plusDays(PRAZO_PADRAO_DIAS)
        );

        return emprestimoRepository.save(emprestimo);
    }

    // ====================================================================
    // DEVOLVER LIVRO
    // ====================================================================
    @Transactional
    public void devolverLivro(Long emprestimoId) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        if (emprestimo.getStatus() != Emprestimo.Status.ATIVO) {
            throw new RegraNegocioException("Este empréstimo já foi finalizado.");
        }

        emprestimo.encerrar();
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();

        if (livro == null) return;

        StatusLivro statusDisponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status 'DISPONIVEL' não existe."));

        StatusLivro statusEmprestado = statusLivroRepository.findByNomeIgnoreCase("EMPRESTADO")
                .orElseThrow(() -> new RegraNegocioException("Status 'EMPRESTADO' não existe."));

        StatusLivro statusReservado = statusLivroRepository.findByNomeIgnoreCase("RESERVADO")
                .orElseThrow(() -> new RegraNegocioException("Status 'RESERVADO' não existe."));

        // Fila de reservas ordenada
        List<Reserva> fila = reservaRepository
        .findByLivroAndStatusOrderByDtInicioReservaAsc(livro, Reserva.ReservaStatus.ATIVA);

        boolean emprestado = false;

        for (Reserva reserva : fila) {

            Usuario usuario = reserva.getUsuario();

            int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(usuario, Emprestimo.Status.ATIVO);
            int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, Reserva.ReservaStatus.ATIVA);

            if (usuario.isFlagAtivo() && emprestimosAtivos + reservasAtivas < 3) {

                Emprestimo novoEmprestimo = new Emprestimo(usuario, livro);
                novoEmprestimo.setDtInicio(LocalDate.now());
                novoEmprestimo.setDtPrevistaDevolucao(LocalDate.now().plusDays(PRAZO_PADRAO_DIAS));
                novoEmprestimo.setStatus(Emprestimo.Status.ATIVO);

                livro.setStatus(statusEmprestado);

                emprestimoRepository.save(novoEmprestimo);

                reserva.setStatus(Reserva.ReservaStatus.CONFIRMADA);
                reservaRepository.save(reserva);

                emprestado = true;
                break;
            }
        }

        if (!emprestado) {
            livro.setStatus(fila.isEmpty() ? statusDisponivel : statusReservado);
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
        LocalDate hoje = LocalDate.now();
        return emprestimoRepository.findAll().stream()
                .filter(e -> e.getStatus() == Emprestimo.Status.ATIVO)
                .filter(e -> e.getDtPrevistaDevolucao() != null &&
                        e.getDtPrevistaDevolucao().isBefore(hoje))
                .collect(Collectors.toList());
    }

    public int countEmprestimosAtivos(Usuario usuario) {
        return emprestimoRepository.countByUsuarioAndStatus(usuario, Emprestimo.Status.ATIVO);
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

        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        if (!pessoa.getUsername().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode realizar empréstimos em seu próprio nome.");
        }
    }

    public void devolverAutorizado(Long emprestimoId, String username) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = pessoa.getRoleString();

        if (role.equalsIgnoreCase("USUARIO") &&
                !emprestimo.getUsuario().getUsername().equals(username)) {

            throw new AccessDeniedException("Você não pode devolver empréstimos de outro usuário.");
        }

        devolverLivro(emprestimoId);
    }
}
