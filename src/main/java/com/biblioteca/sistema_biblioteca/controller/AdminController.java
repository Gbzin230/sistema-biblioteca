package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.FuncionarioAdminRequestDTO;
import com.biblioteca.sistema_biblioteca.model.Admin;
import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.model.Role;
import com.biblioteca.sistema_biblioteca.repository.RoleRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UsuarioRepository usuarioRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // CRIAR FUNCIONÁRIO
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/criar-funcionario")
    public ResponseEntity<?> criarFuncionario(@Valid @RequestBody FuncionarioAdminRequestDTO dto) {

        Role funcRole = roleRepository.findByNomeIgnoreCase("FUNCIONARIO")
                .orElseThrow(() -> new RuntimeException("Role FUNCIONARIO não encontrada"));

        Funcionario f = new Funcionario();
        f.setUsername(dto.getUsername());
        f.setSenha(passwordEncoder.encode(dto.getSenha()));
        f.setNome(dto.getNome());
        f.setEmail(dto.getEmail());
        f.setTelefone(dto.getTelefone());
        f.setCpf(dto.getCpf());
        f.setEndereco(dto.getEndereco());
        f.setSexo(dto.getSexo());
        f.setFlagAtivo(true);
        f.setRole(funcRole);

        usuarioRepository.save(f);  // usa tabela tb_usuario
        return ResponseEntity.ok("Funcionário criado com sucesso!");
    }

    // =========================================================
    // CRIAR ADMIN
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/criar-admin")
    public ResponseEntity<?> criarAdmin(@Valid @RequestBody FuncionarioAdminRequestDTO dto) {

        Role adminRole = roleRepository.findByNomeIgnoreCase("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role ADMIN não encontrada"));

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
        a.setRole(adminRole);

        usuarioRepository.save(a);
        return ResponseEntity.ok("Administrador criado com sucesso!");
    }
}
