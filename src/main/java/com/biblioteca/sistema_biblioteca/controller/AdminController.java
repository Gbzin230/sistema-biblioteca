package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.model.Admin;
import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.repository.AdminRepository;
import com.biblioteca.sistema_biblioteca.repository.FuncionarioRepository;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminRepository adminRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepository adminRepository,
                           FuncionarioRepository funcionarioRepository,
                           PessoaRepository pessoaRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/criar-funcionario")
    public ResponseEntity<?> criarFuncionario(@RequestParam String username,
                                              @RequestParam String senha,
                                              @RequestParam String nome) {
        Funcionario f = new Funcionario();
        f.setUsername(username);
        f.setSenha(passwordEncoder.encode(senha));
        f.setNome(nome);
        f.setFlagAtivo(true);
        funcionarioRepository.save(f);
        return ResponseEntity.ok("Funcionário criado com sucesso!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/criar-admin")
    public ResponseEntity<?> criarAdmin(@RequestParam String username,
                                        @RequestParam String senha,
                                        @RequestParam String nome) {
        Admin a = new Admin();
        a.setUsername(username);
        a.setSenha(passwordEncoder.encode(senha));
        a.setNome(nome);
        a.setFlagAtivo(true);
        adminRepository.save(a);
        return ResponseEntity.ok("Admin criado com sucesso!");
    }
}
