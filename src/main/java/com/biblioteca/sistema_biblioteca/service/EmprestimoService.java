package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final PessoaRepository pessoaRepository;

    private static final int PRAZO_PADRAO_DIAS = 7;
    private static final int MAX_RENOVACOES = 2;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             LivroRepository livroRepository,
                             UsuarioRepository usuarioRepository,
                             ReservaRepository reservaRepository,
                             PessoaRepository pessoaRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public Emprestimo realizarEmprestimo(Long usuarioId, Long livroId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));

        int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(usuario, Emprestimo.Status.ATIVO);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, Reserva.ReservaStatus.ATIVA);
        int totalSlots = emprestimosAtivos + reservasAtivas;

        if (totalSlots >= 3) {
            throw new RegraNegocioException("Usuário atingiu o limite máximo de 3 slots (empréstimos + reservas).");
        }

        if (Boolean.FALSE.equals(usuario.isFlagAtivo())) {
            throw new RegraNegocioException("Usuário bloqueado não pode realizar empréstimos.");
        }

        if (!livro.isDisponivel() && livro.getStatus() != Livro.Status.RESERVADO) {
            throw new RegraNegocioException("Livro não está disponível para empréstimo.");
        }

        Emprestimo emprestimo = new Emprestimo(usuario, livro);
        emprestimo.setStatus(Emprestimo.Status.ATIVO);
        emprestimo.setDtInicio(LocalDate.now());
        emprestimo.setDtPrevistaDevolucao(LocalDate.now().plusDays(PRAZO_PADRAO_DIAS));

        livro.alterarStatus(Livro.Status.EMPRESTADO);
        livroRepository.save(livro);

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo renovarEmprestimo(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        if (emprestimo.getStatus() != Emprestimo.Status.ATIVO) {
            throw new RegraNegocioException("Apenas empréstimos ATIVOS podem ser renovados.");
        }

        if (emprestimo.getNumRenovacoes() == null) {
            emprestimo.setNumRenovacoes(0);
        }

        if (emprestimo.getNumRenovacoes() >= MAX_RENOVACOES) {
            throw new RegraNegocioException("Máximo de renovações atingido.");
        }

        boolean renovou = emprestimo.renovar();
        if (!renovou) {
            emprestimo.setDtPrevistaDevolucao(emprestimo.getDtPrevistaDevolucao().plusDays(PRAZO_PADRAO_DIAS * 2));
            emprestimo.setNumRenovacoes(emprestimo.getNumRenovacoes() + 1);
        }

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public void devolverLivro(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        if (emprestimo.getStatus() != Emprestimo.Status.ATIVO) {
            throw new RegraNegocioException("Este empréstimo já foi finalizado ou não está ativo.");
        }

        emprestimo.encerrar();
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();

        if (livro != null) {
            List<Reserva> fila = reservaRepository.findByLivroAndStatusOrderByDtSolicitacaoAsc(livro, Reserva.ReservaStatus.ATIVA);

            boolean emprestado = false;
            for (Reserva reserva : fila) {
                Usuario usuario = reserva.getUsuario();
                int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(usuario, Emprestimo.Status.ATIVO);
                int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, Reserva.ReservaStatus.ATIVA);
                int totalSlots = emprestimosAtivos + reservasAtivas;

                if (Boolean.TRUE.equals(usuario.isFlagAtivo()) && totalSlots < 3) {
                    Emprestimo novoEmprestimo = new Emprestimo(usuario, livro);
                    livro.alterarStatus(Livro.Status.EMPRESTADO);
                    emprestimoRepository.save(novoEmprestimo);
                    reserva.setStatus(Reserva.ReservaStatus.CONFIRMADA);
                    reservaRepository.save(reserva);
                    emprestado = true;
                    break;
                }
            }

            if (!emprestado) {
                if (fila.isEmpty()) {
                    livro.alterarStatus(Livro.Status.DISPONIVEL);
                } else {
                    livro.alterarStatus(Livro.Status.RESERVADO);
                }
            }

            livroRepository.save(livro);
        }
    }

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
                .filter(e -> e.getDtPrevistaDevolucao() != null && e.getDtPrevistaDevolucao().isBefore(hoje))
                .collect(Collectors.toList());
    }

    public int countEmprestimosAtivos(Usuario usuario) {
        return emprestimoRepository.countByUsuarioAndStatus(usuario, Emprestimo.Status.ATIVO);
    }

    // 🔐 Verifica se o usuário autenticado é o dono do empréstimo
    public void validarDonoDoEmprestimo(Long emprestimoId, String username) {
        Emprestimo e = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));
        if (!e.getUsuario().getUsername().equals(username)) {
            throw new AccessDeniedException("Você não pode acessar empréstimos de outro usuário.");
        }
    }

    // 🔐 Valida se o usuário logado é o mesmo do empréstimo
    public void validarUsuarioEmprestimo(Long usuarioId, String username) {
        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        if (!pessoa.getId().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode realizar empréstimos em seu próprio nome.");
        }
    }

    // 🔐 Permite devolver apenas se for o dono ou ADMIN/FUNCIONÁRIO
    public void devolverAutorizado(Long emprestimoId, String username) {
        Emprestimo e = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = pessoa.getRoleString();

        if (role.equalsIgnoreCase("USUARIO") &&
                !e.getUsuario().getUsername().equals(username)) {
            throw new AccessDeniedException("Você não pode devolver empréstimos de outro usuário.");
        }

        devolverLivro(emprestimoId); // chama o método original
    }

}