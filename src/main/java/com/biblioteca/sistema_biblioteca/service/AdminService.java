package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.biblioteca.sistema_biblioteca.model.Admin;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.repository.AdminRepository;
import com.biblioteca.sistema_biblioteca.repository.FuncionarioRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;

    public AdminService(AdminRepository adminRepository,
            FuncionarioRepository funcionarioRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            PessoaRepository pessoaRepository) {
        this.adminRepository = adminRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
    }

    // CRUD
    public Admin salvarAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public List<Admin> listarAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> buscarAdminPorId(Long id) {
        return adminRepository.findById(id);
    }

    public void deletarAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    // Métodos Administrativos

    // Funcionário
    public Funcionario cadastrarFuncionario(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }

    public void bloquearPessoa(String username) {
        pessoaRepository.findByUsername(username).ifPresent(p -> {

            p.setFlagAtivo(false);
            pessoaRepository.save(p);

        });

    }

    public void forcarDesalocacao(Emprestimo emprestimo) {
        if (emprestimo != null) {
            emprestimo.getUsuario().getLivrosAtivos().remove(emprestimo);
            emprestimo.getLivro().alterarStatus(Livro.Status.DISPONIVEL);
            pessoaRepository.save((emprestimo.getUsuario()));
        }
    }

}
