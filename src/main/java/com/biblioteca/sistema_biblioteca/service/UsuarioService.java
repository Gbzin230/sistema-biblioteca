package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.sistema_biblioteca.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.dto.PessoaUpdateDTO;
import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmprestimoRepository emprestimoRepository,
            ReservaRepository reservaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
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
    // 🟢 SALVAR USUÁRIO (com validações)
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

    public Optional<Usuario> buscaPorId(String username) {
        return usuarioRepository.findById(username);
    }

    // ============================================================
    // DOMÍNIO (EMPRÉSTIMO, RESERVA, ETC)
    // ============================================================

    public Emprestimo emprestarLivro(String username, Livro livro) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Emprestimo emprestimo = usuario.emprestarLivro(livro);
        usuarioRepository.save(usuario);
        return emprestimo;
    }

    public void devolverLivro(String username, Emprestimo emprestimo) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuario.devolverLivro(emprestimo);
        usuarioRepository.save(usuario);
    }

    public Reserva reservarLivro(String username, Livro livro) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Reserva reserva = usuario.reservarLivro(livro);
        usuarioRepository.save(usuario);
        return reserva;
    }

    public void cancelarReserva(String username, Livro livro) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuario.cancelarReserva(livro);
        usuarioRepository.save(usuario);
    }

    public List<Emprestimo> consultaHistorico(String username) {
        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return usuario.consultaHistorico();
    }

    // ============================================================
    // GESTÃO ADMINISTRATIVA
    // ============================================================

    @Transactional
    public Usuario aprovarUsuario(String username) {
        Usuario u = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        u.setFlagAtivo(true);
        return usuarioRepository.save(u);
    }

    @Transactional
    public Usuario bloquearUsuario(String username) {
        Usuario u = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        u.setFlagAtivo(false);
        return usuarioRepository.save(u);
    }

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

        usuarioRepository.flush();
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public Usuario atualizarUsuario(PessoaUpdateDTO dto, String identificador) {

        // procurar por username OU email OU cpf
        Usuario usuario = usuarioRepository
                .findByUsernameOrEmailOrCpf(identificador, identificador, identificador)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        // Atualiza somente os campos enviados
        if (dto.getNome() != null) usuario.setNome(dto.getNome());
        if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
        if (dto.getTelefone() != null) usuario.setTelefone(dto.getTelefone());
        if (dto.getCpf() != null) usuario.setCpf(dto.getCpf());
        if (dto.getEndereco() != null) usuario.setEndereco(dto.getEndereco());
        if (dto.getSexo() != null) usuario.setSexo(Character.toUpperCase(dto.getSexo()));
        if (dto.getDtNascimento() != null) usuario.setDtNascimento(dto.getDtNascimento().toString());
        if (dto.getSenha() != null) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }


}


