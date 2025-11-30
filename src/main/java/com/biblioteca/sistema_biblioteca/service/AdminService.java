package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Role;
import com.biblioteca.sistema_biblioteca.model.StatusLivro;

import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.RoleRepository;
import com.biblioteca.sistema_biblioteca.repository.StatusLivroRepository;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final RoleRepository roleRepository;
    private final StatusLivroRepository statusLivroRepository;

    public AdminService(
            UsuarioRepository usuarioRepository,
            LivroRepository livroRepository,
            RoleRepository roleRepository,
            StatusLivroRepository statusLivroRepository) {

        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.roleRepository = roleRepository;
        this.statusLivroRepository = statusLivroRepository;
    }

    // ===============================================
    // ADMIN
    // ===============================================
    public Usuario salvarAdmin(Usuario usuario) {

        Role adminRole = roleRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Role ADMIN não encontrada"));

        usuario.setRole(adminRole);
        usuario.setFlagAtivo(true);

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarAdmins() {
        Role adminRole = roleRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Role ADMIN não encontrada"));

        return usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != null &&
                        u.getRole().getId().equals(adminRole.getId()))
                .toList();
    }

    public Optional<Usuario> buscarAdminPorId(String username) {
        return usuarioRepository.findById(username);
    }

    public void deletarAdmin(String username) {
        usuarioRepository.deleteById(username);
    }

    // ===============================================
    // FUNCIONÁRIO
    // ===============================================

    public Usuario cadastrarFuncionario(Usuario usuario) {

        Role funcRole = roleRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Role FUNCIONARIO não encontrada"));

        usuario.setRole(funcRole);
        usuario.setFlagAtivo(true);

        return usuarioRepository.save(usuario);
    }

    // ===============================================
    // BLOQUEIO / DESATIVAÇÃO
    // ===============================================

    public void bloquearPessoa(String username) {
        usuarioRepository.findById(username).ifPresent(user -> {
            user.setFlagAtivo(false);
            usuarioRepository.save(user);
        });
    }

    // ===============================================
    // FORÇAR DESALOCAÇÃO / DEVOLUÇÃO
    // ===============================================

    public void forcarDesalocacao(Emprestimo emprestimo) {
        if (emprestimo == null) return;

        Livro livro = emprestimo.getLivro();

        if (livro != null) {

            // 1) Atualiza status do livro → DISPONÍVEL
            StatusLivro disponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                    .orElseThrow(() -> new RuntimeException("Status DISPONIVEL não encontrado"));

            livro.setStatus(disponivel);
            livroRepository.save(livro);

            // 2) Atualiza status do empréstimo (opcional)
            if (emprestimo.getStatus() != null) {
                emprestimo.getStatus().setNome("FINALIZADO");
            }

            // 3) IMPORTANTE: NÃO chama usuario.devolverLivro(),
            // para evitar problemas com coleções LAZY.
        }
    }
}
