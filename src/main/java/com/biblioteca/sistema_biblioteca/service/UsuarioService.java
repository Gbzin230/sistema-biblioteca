package com.biblioteca.sistema_biblioteca.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.biblioteca.sistema_biblioteca.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.StatusEmprestimo;
import com.biblioteca.sistema_biblioteca.model.StatusReserva;
import com.biblioteca.sistema_biblioteca.model.StatusUsuario;
import com.biblioteca.sistema_biblioteca.model.Usuario;

import com.biblioteca.sistema_biblioteca.dto.PessoaUpdateDTO;
import com.biblioteca.sistema_biblioteca.dto.UsuarioListagemDTO;
import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatusReservaRepository statusReservaRepository;
    private final StatusUsuarioRepository statusUsuarioRepository;
    private final StatusEmprestimoRepository statusEmprestimoRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmprestimoRepository emprestimoRepository,
            ReservaRepository reservaRepository,
            PasswordEncoder passwordEncoder,
            StatusReservaRepository statusReservaRepository,
            StatusUsuarioRepository statusUsuarioRepository,
            StatusEmprestimoRepository statusEmprestimoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
        this.statusReservaRepository = statusReservaRepository;
        this.statusUsuarioRepository = statusUsuarioRepository;
        this.statusEmprestimoRepository = statusEmprestimoRepository;
    }

    // ============================================================
    // 🔐 PASSWORD ENCODE
    // ============================================================
    public String encodePassword(String senha) {
        return passwordEncoder.encode(senha);
    }

    // ============================================================
    // ✔ VALIDAÇÕES
    // ============================================================

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsById(username);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    // ============================================================
    // SALVAR
    // ============================================================
    public Usuario salvar(Usuario usuario) {

        if (existsByUsername(usuario.getUsername())) {
            throw new RegraNegocioException("Nome de usuário já existe.");
        }

        if (existsByEmail(usuario.getEmail())) {
            throw new RegraNegocioException("Email já cadastrado.");
        }

        return usuarioRepository.save(usuario);
    }

    // ============================================================
    // LISTAR / BUSCAR
    // ============================================================

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RegraNegocioException("Usuário não autenticado.");
        }

        String username = auth.getName();

        return usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário logado não encontrado."));
    }

    public Optional<Usuario> buscaPorId(String username) {
        return usuarioRepository.findById(username);
    }

    public List<UsuarioListagemDTO> listarUsuariosCompleto() {
        return usuarioRepository.findAll().stream().map(u ->
            new UsuarioListagemDTO(
                    u.getUsername(),
                    statusUsuarioRepository.findById(u.getCodStatus())
                            .map(StatusUsuario::getNomeStatus)
                            .orElse("DESCONHECIDO"),
                    u.getUrlDocumento(),
                    u.getDtNascimento(),
                    u.getEndereco(),
                    u.getCep(),
                    u.getCpf(),
                    u.getTelefone(),
                    u.getEmail(),
                    u.getNome(),
                    u.getDtCadastro(),
                    u.getSexo(),
                    u.getDtDesativacao(),
                    u.getDtBanimento(),
                    u.getLimiteSlots(),
                    u.getUrlCapa(),
                    u.getFlagAtivo(),
                    u.getRole() != null ? u.getRole().getNome() : null
            )
        ).toList();
    }

    // ============================================================
    // DOMÍNIO
    // ============================================================

    public Emprestimo emprestarLivro(String username, Livro livro) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));

        Emprestimo emprestimo = usuario.emprestarLivro(livro);
        return usuarioRepository.save(usuario).getLivrosAtivos()
                .stream().filter(e -> e.getLivro().equals(livro)).findFirst()
                .orElse(emprestimo);
    }

    public void devolverLivro(String username, Emprestimo emprestimo) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));

        usuario.devolverLivro(emprestimo);
        usuarioRepository.save(usuario);
    }

    public Reserva reservarLivro(String username, Livro livro) {

        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status 'ATIVA' não existe."));

        Reserva reserva = usuario.reservarLivro(livro, ativa);
        usuarioRepository.save(usuario);

        return reserva;
    }

    public void cancelarReserva(String username, Livro livro) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));

        usuario.cancelarReserva(livro);
        usuarioRepository.save(usuario);
    }

    public List<Emprestimo> consultaHistorico(String username) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));

        return usuario.consultaHistorico();
    }

    // ============================================================
    // GESTÃO ADMINISTRATIVA
    // ============================================================

    @Transactional
    public Usuario aprovarUsuario(String username) {
        Usuario u = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        if (u.getFlagAtivo() != null && !u.getFlagAtivo() &&
                u.getCodStatus() != null && u.getCodStatus().equals(3)) {
            throw new RegraNegocioException("Usuário Bloqueado não pode ser aprovado.");
        } else {
            u.setFlagAtivo(true);
            u.setCodStatus(2);
        }

        return usuarioRepository.save(u);
    }

    // ============================================================
    // 🔥 BLOQUEAR USUÁRIO (corrigido)
    // ============================================================
    @Transactional
    public Usuario bloquearUsuario(String username) {

        Usuario u = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        // -------- cancelamento das reservas ativas --------
        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVA não existe."));

        StatusReserva cancelada = statusReservaRepository.findByNomeIgnoreCase("CANCELADA")
                .orElseThrow(() -> new RegraNegocioException("Status CANCELADA não existe."));

        List<Reserva> reservasAtivas = reservaRepository.findByUsuarioOrderByDtInicioReservaDesc(u)
                .stream()
                .filter(r -> r.getStatus().getId().equals(ativa.getId()))
                .toList();

        for (Reserva r : reservasAtivas) {
            r.setStatus(cancelada);
            reservaRepository.save(r);
        }

        // -------- finalizar empréstimos ativos --------
        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não existe."));

        StatusEmprestimo finalizado = statusEmprestimoRepository.findByNomeIgnoreCase("FINALIZADO")
                .orElseThrow(() -> new RegraNegocioException("Status FINALIZADO não existe."));

        List<Emprestimo> emprestimosAtivos = emprestimoRepository.findByUsuarioAndStatus(u, ativo);

        for (Emprestimo e : emprestimosAtivos) {
            e.setStatus(finalizado);
            e.setDtFim(LocalDateTime.now());
            emprestimoRepository.save(e);
        }

        // -------- bloquear usuário --------
        u.setFlagAtivo(false);
        u.setCodStatus(3);

        return usuarioRepository.save(u);
    }

    // ============================================================
    // 🔥 BLOQUEIO EM MASSA (corrigido)
    // ============================================================
    @Transactional
    public int bloquearUsuariosEmMassa(List<String> usernames) {
        int count = 0;

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVA não existe."));

        StatusReserva cancelada = statusReservaRepository.findByNomeIgnoreCase("CANCELADA")
                .orElseThrow(() -> new RegraNegocioException("Status CANCELADA não existe."));

        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não existe."));

        StatusEmprestimo finalizado = statusEmprestimoRepository.findByNomeIgnoreCase("FINALIZADO")
                .orElseThrow(() -> new RegraNegocioException("Status FINALIZADO não existe."));

        for (String username : usernames) {
            try {
                Usuario u = usuarioRepository.findById(username)
                        .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado: " + username));

                // cancelar reservas
                List<Reserva> reservasAtivas = reservaRepository.findByUsuarioOrderByDtInicioReservaDesc(u)
                        .stream()
                        .filter(r -> r.getStatus().getId().equals(ativa.getId()))
                        .toList();

                for (Reserva r : reservasAtivas) {
                    r.setStatus(cancelada);
                    reservaRepository.save(r);
                }

                // finalizar empréstimos
                List<Emprestimo> emprestimosAtivos = emprestimoRepository.findByUsuarioAndStatus(u, ativo);

                for (Emprestimo e : emprestimosAtivos) {
                    e.setStatus(finalizado);
                    e.setDtFim(LocalDateTime.now());
                    emprestimoRepository.save(e);
                }

                // bloquear usuário
                u.setFlagAtivo(false);
                u.setCodStatus(3);
                usuarioRepository.save(u);

                count++;

            } catch (Exception ignored) {}
        }

        return count;
    }

    // ============================================================
    // DESBLOQUEAR
    // ============================================================
    @Transactional
    public Usuario desbloquearUsuario(String username) {
        Usuario u = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        u.setFlagAtivo(true);
        u.setCodStatus(2);

        return usuarioRepository.save(u);
    }

    // ============================================================
    // DESBLOQUEIO EM MASSA
    // ============================================================
    @Transactional
    public int desbloquearUsuariosEmMassa(List<String> usernames) {
        int count = 0;

        for (String username : usernames) {
            try {
                Usuario u = usuarioRepository.findById(username)
                        .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado: " + username));

                u.setFlagAtivo(true);
                u.setCodStatus(2);
                usuarioRepository.save(u);
                count++;

            } catch (Exception ignored) {}
        }

        return count;
    }

    // ============================================================
    // DELETAR
    // ============================================================
    @Transactional
    public void deletar(String username) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        boolean temEmprestimo = emprestimoRepository.existsByUsuario(usuario);
        boolean temReserva = reservaRepository.existsByUsuario(usuario);

        if (temEmprestimo || temReserva) {
            throw new RegraNegocioException(
                    "Não é possível deletar o usuário. Ele possui histórico de empréstimos ou reservas."
            );
        }

        usuarioRepository.delete(usuario);
    }

    // ============================================================
    // ATUALIZAR
    // ============================================================
    @Transactional
    public Usuario atualizarUsuario(PessoaUpdateDTO dto, String identificador) {

        Usuario usuario = usuarioRepository
                .findByUsernameOrEmailOrCpf(identificador, identificador, identificador)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        Usuario logado = getUsuarioLogado();

        boolean isAdmin = logado.getRole().getNome().equalsIgnoreCase("ADMIN");
        boolean isSelf = logado.getUsername().equals(usuario.getUsername());

        if (!isAdmin && !isSelf) {
            throw new RegraNegocioException("Você não pode alterar dados de outros usuários.");
        }

        if (!isAdmin) {

            if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
            if (dto.getTelefone() != null) usuario.setTelefone(dto.getTelefone());
            if (dto.getCep() != null) usuario.setCep(dto.getCep());
            if (dto.getEndereco() != null) usuario.setEndereco(dto.getEndereco());
            if (dto.getSenha() != null) usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

            return usuarioRepository.save(usuario);
        }

        if (dto.getNome() != null) usuario.setNome(dto.getNome());
        if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
        if (dto.getTelefone() != null) usuario.setTelefone(dto.getTelefone());
        if (dto.getCpf() != null) usuario.setCpf(dto.getCpf());
        if (dto.getCep() != null) usuario.setCep(dto.getCep());
        if (dto.getEndereco() != null) usuario.setEndereco(dto.getEndereco());
        if (dto.getSexo() != null) usuario.setSexo(Character.toUpperCase(dto.getSexo()));

        if (dto.getDtNascimento() != null) {
            usuario.setDtNascimento(dto.getDtNascimento().toString());
        }

        if (dto.getSenha() != null) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }
}
