package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.FuncionarioAdminRequestDTO;
import com.biblioteca.sistema_biblioteca.dto.TrocarCargoDTO;
import com.biblioteca.sistema_biblioteca.model.Admin;
import com.biblioteca.sistema_biblioteca.model.Role;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.RoleRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            UsuarioRepository usuarioRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =====================================================================
    // 🟢 CADASTRAR FUNCIONÁRIO
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastrar-funcionario")
    public ResponseEntity<?> cadastrarFuncionario(
            @Valid @RequestBody FuncionarioAdminRequestDTO dto
    ) {

        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(
                    Map.of("erro", "Username já está em uso.")
            );
        }

        Role funcRole = roleRepository.findByNomeIgnoreCase("FUNCIONARIO")
                .orElseThrow(() -> new RuntimeException("Role FUNCIONARIO não encontrada"));

        Usuario funcionario = new Usuario();
        funcionario.setUsername(dto.getUsername());
        funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setTelefone(dto.getTelefone());
        funcionario.setCpf(dto.getCpf());
        funcionario.setEndereco(dto.getEndereco());
        funcionario.setSexo(dto.getSexo());
        funcionario.setFlagAtivo(true);
        funcionario.setRole(funcRole);
        funcionario.setCodStatus(2); // Ativo por padrão

        usuarioRepository.save(funcionario);

        return ResponseEntity.ok(
                Map.of(
                        "mensagem", "Funcionário cadastrado com sucesso!",
                        "usuario", funcionario.getUsername(),
                        "cargo", "FUNCIONARIO"
                )
        );
    }


    // =====================================================================
    // 🔥 TROCAR CARGO (promover, rebaixar)
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/trocar-cargo")
    public ResponseEntity<?> trocarCargo(
            @Valid @RequestBody TrocarCargoDTO dto
    ) {

        Usuario usuario = usuarioRepository.findByUsername(dto.username())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!usuario.getCpf().equals(dto.cpf())) {
            return ResponseEntity.badRequest().body(
                    Map.of("erro", "CPF não confere com o usuário.")
            );
        }

        Role novoRole = roleRepository.findByNomeIgnoreCase(dto.novoRole())
                .orElseThrow(() -> new RuntimeException("Cargo inválido."));

        usuario.setRole(novoRole);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(
                Map.of(
                        "mensagem", "Cargo atualizado com sucesso!",
                        "username", usuario.getUsername(),
                        "cpf", usuario.getCpf(),
                        "novoCargo", novoRole.getNome()
                )
        );
    }
}
