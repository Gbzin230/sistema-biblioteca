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
    private final UsuarioService usuarioService; // service que contém lógica de deletar e aprovar

    public PessoaController(PessoaRepository pessoaRepository,
                            PasswordEncoder passwordEncoder,
                            ModelMapper modelMapper,
                            UsuarioService usuarioService) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.usuarioService = usuarioService;
    }

    // Cadastro público
    @PostMapping
    public ResponseEntity<PessoaResponseDTO> cadastrar(@Valid @RequestBody PessoaRequestDTO dto) {
        Usuario usuario = modelMapper.map(dto, Usuario.class);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        // Ajustes de campos que exigem conversão ou não são mapeados automaticamente
        if (dto.getSexo() != null && !dto.getSexo().isEmpty()) {
            usuario.setSexo(dto.getSexo().charAt(0));
        }

        // 🔒 Por segurança: novos usuários ficam inativos até aprovação
        usuario.setFlagAtivo(false);

        // Garante que sempre é um usuário comum
        Usuario salvo = pessoaRepository.save(usuario);

        PessoaResponseDTO response = modelMapper.map(salvo, PessoaResponseDTO.class);
        return ResponseEntity.ok(response);
    }

    // listar - apenas FUNCIONARIO ou ADMIN
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<List<PessoaResponseDTO>> listar() {
        List<PessoaResponseDTO> pessoas = pessoaRepository.findAll()
                .stream()
                .map(p -> modelMapper.map(p, PessoaResponseDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pessoas);
    }

    // bloquear - ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(@PathVariable Long id) {
        usuarioService.bloquearUsuario(id); // implementado no service
        return ResponseEntity.ok().build();
    }

    // deletar - ADMIN (ou preferir soft-delete)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
