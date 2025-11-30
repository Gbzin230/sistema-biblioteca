package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.sistema_biblioteca.dto.UsuarioListagemDTO;
import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Role;
import com.biblioteca.sistema_biblioteca.model.StatusLivro;
import com.biblioteca.sistema_biblioteca.model.StatusUsuario;
import com.biblioteca.sistema_biblioteca.model.Usuario;

import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.StatusLivroRepository;
import com.biblioteca.sistema_biblioteca.repository.StatusUsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.RoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FuncionarioService {

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final StatusLivroRepository statusLivroRepository;
    private final RoleRepository roleRepository;
    private final StatusUsuarioRepository statusUsuarioRepository;

    public FuncionarioService(
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            StatusLivroRepository statusLivroRepository,
            RoleRepository roleRepository,
            StatusUsuarioRepository statusUsuarioRepository
    ) {
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.statusLivroRepository = statusLivroRepository;
        this.roleRepository = roleRepository;
        this.statusUsuarioRepository = statusUsuarioRepository;
    }

    // ============================================================
    // FUNCIONÁRIOS (sem tabela dedicada)
    // ============================================================

    public Usuario salvarFuncionario(Usuario funcionario) {

        Role roleFuncionario = roleRepository.findByNomeIgnoreCase("FUNCIONARIO")
                .orElseThrow(() -> new RegraNegocioException("Role FUNCIONARIO não encontrada."));

        funcionario.setRole(roleFuncionario);
        funcionario.setFlagAtivo(true);

        return usuarioRepository.save(funcionario);
    }

    public List<Usuario> listarFuncionarios() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() != null &&
                             "FUNCIONARIO".equalsIgnoreCase(u.getRole().getNome()))
                .toList();
    }

    public Optional<Usuario> buscarFuncionarioPorId(String username) {
        return usuarioRepository.findById(username)
                .filter(u -> u.getRole() != null &&
                             "FUNCIONARIO".equalsIgnoreCase(u.getRole().getNome()));
    }

    public void deletarFuncionario(String username) {
        usuarioRepository.findById(username)
                .filter(u -> u.getRole() != null &&
                             "FUNCIONARIO".equalsIgnoreCase(u.getRole().getNome()))
                .ifPresent(u -> usuarioRepository.deleteById(username));
    }

    // ============================================================
    // LIVROS
    // ============================================================

    @Transactional
    public Livro cadastrarLivro(Livro livro) {

        livro.setFlagAtivo(true);

        StatusLivro statusDisponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status DISPONIVEL não encontrado."));

        livro.setStatus(statusDisponivel);

        return livroRepository.save(livro);
    }

    @Transactional
    public void inativarLivro(Livro livro) {

        livro.setFlagAtivo(false);

        StatusLivro statusIndisponivel = statusLivroRepository.findByNomeIgnoreCase("INDISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status INDISPONIVEL não encontrado."));

        livro.setStatus(statusIndisponivel);

        livroRepository.save(livro);
    }

    @Transactional
    public void ativarLivro(Livro livro) {

        livro.setFlagAtivo(true);

        StatusLivro statusDisponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status DISPONIVEL não encontrado."));

        livro.setStatus(statusDisponivel);

        livroRepository.save(livro);
    }

    public List<Livro> consultarLivros() {
        return livroRepository.findAll();
    }

    // ============================================================
    // USUÁRIOS
    // ============================================================

    public List<UsuarioListagemDTO> consultarFuncionarios() {

        List<Integer> rolesPermitidos = List.of(1, 2); // ADMIN e FUNCIONARIO

        return usuarioRepository.findByRoleIdIn(rolesPermitidos)
                .stream()
                .map(u -> new UsuarioListagemDTO(
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
                ))
                .toList();
    }

    public List<Livro> consultarHistoricoUsuario(Usuario usuario) {
        return usuario.consultaHistorico()
                .stream()
                .map(Emprestimo::getLivro)
                .toList();
    }

    public boolean aprovarUsuario(Usuario usuario) {
        usuario.setFlagAtivo(true);
        usuarioRepository.save(usuario);
        return true;
    }

    // ============================================================
    // APROVAR / RECUSAR EM MASSA
    // ============================================================

    @Transactional
    public int aprovarFuncionariosEmMassa(List<String> usernames) {

        int count = 0;

        for (String username : usernames) {
            try {
                Usuario u = usuarioRepository.findById(username)
                        .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado: " + username));

                if (u.getCodStatus() == null || u.getCodStatus() != 1) {
                    continue;
                }

                u.setFlagAtivo(true);
                u.setCodStatus(2); // APROVADO
                usuarioRepository.save(u);
                count++;

            } catch (Exception ignored) {}
        }

        return count;
    }

    @Transactional
    public int recusarFuncionariosEmMassa(List<String> usernames) {

        int count = 0;

        for (String username : usernames) {
            try {
                Usuario u = usuarioRepository.findById(username)
                        .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado: " + username));

                if (u.getCodStatus() == null || u.getCodStatus() != 1) {
                    continue;
                }

                usuarioRepository.delete(u);
                count++;

            } catch (Exception ignored) {}
        }

        return count;
    }

}
