package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.FuncionarioAdminRequestDTO;
import com.biblioteca.sistema_biblioteca.model.Admin;
import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.repository.AdminRepository;
import com.biblioteca.sistema_biblioteca.repository.FuncionarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminRepository adminRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepository adminRepository,
                           FuncionarioRepository funcionarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/criar-funcionario")
    public ResponseEntity<?> criarFuncionario(@Valid @RequestBody FuncionarioAdminRequestDTO dto) {
        Funcionario f = new Funcionario();
        f.setUsername(dto.getUsername());
        f.setSenha(passwordEncoder.encode(dto.getSenha()));
        f.setNome(dto.getNome());
        f.setEmail(dto.getEmail());
        f.setTelefone(dto.getTelefone());
        f.setCpf(dto.getCpf());
        f.setEndereco(dto.getEndereco());
        f.setSexo(dto.getSexo().charAt(0));
        f.setFlagAtivo(true);

        funcionarioRepository.save(f);
        return ResponseEntity.ok("Funcionário criado com sucesso!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/criar-admin")
    public ResponseEntity<?> criarAdmin(@Valid @RequestBody FuncionarioAdminRequestDTO dto) {
        Admin a = new Admin();
        a.setUsername(dto.getUsername());
        a.setSenha(passwordEncoder.encode(dto.getSenha()));
        a.setNome(dto.getNome());
        a.setEmail(dto.getEmail());
        a.setTelefone(dto.getTelefone());
        a.setCpf(dto.getCpf());
        a.setEndereco(dto.getEndereco());
        a.setSexo(dto.getSexo());
        a.setFlagAtivo(true);

        adminRepository.save(a);
        return ResponseEntity.ok("Administrador criado com sucesso!");
    }
}
