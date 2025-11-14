package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.PessoaRequestDTO;
import com.biblioteca.sistema_biblioteca.dto.PessoaResponseDTO;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import com.biblioteca.sistema_biblioteca.service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UsuarioService usuarioService;

    public PessoaController(PessoaRepository pessoaRepository,
                            PasswordEncoder passwordEncoder,
                            ModelMapper modelMapper,
                            UsuarioService usuarioService) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.usuarioService = usuarioService;
    }

    // ============================================================
    // 🟢 CADASTRO PÚBLICO
    // ============================================================
    @PostMapping
    public ResponseEntity<PessoaResponseDTO> cadastrar(@Valid @RequestBody PessoaRequestDTO dto) {

        Usuario usuario = modelMapper.map(dto, Usuario.class);

        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        if (dto.getSexo() != null && !dto.getSexo().isEmpty()) {
            usuario.setSexo(dto.getSexo().charAt(0));
        }

        // 🔒 Novos usuários aguardam aprovação
        usuario.setFlagAtivo(false);

        Usuario salvo = pessoaRepository.save(usuario);

        PessoaResponseDTO response = modelMapper.map(salvo, PessoaResponseDTO.class);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 📋 LISTAR — FUNCIONARIO ou ADMIN
    // ============================================================
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<List<PessoaResponseDTO>> listar() {

        List<PessoaResponseDTO> pessoas = pessoaRepository.findAll()
                .stream()
                .map(p -> modelMapper.map(p, PessoaResponseDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pessoas);
    }

    // ============================================================
    // ⛔ BLOQUEAR — ADMIN
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{username}/bloquear")
    public ResponseEntity<?> bloquear(@PathVariable String username) {
        usuarioService.bloquearUsuario(username);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ❌ DELETAR — ADMIN
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<?> deletar(@PathVariable String username) {
        usuarioService.deletar(username);
        return ResponseEntity.noContent().build();
    }
}
