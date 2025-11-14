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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public PessoaController(UsuarioService usuarioService, ModelMapper modelMapper) {
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

    // ============================================================
    // 🟢 CADASTRO PÚBLICO
    // ============================================================
    @PostMapping
    public ResponseEntity<PessoaResponseDTO> cadastrar(@Valid @RequestBody PessoaRequestDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setUsername(dto.getUsername());
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuario.setCpf(dto.getCpf());
        usuario.setEndereco(dto.getEndereco());
        usuario.setSexo(dto.getSexo());

        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        usuario.setDtNascimento(dto.getDtNascimento().format(formatter));

        // valores padrão
        usuario.setLimiteSlots(3);
        usuario.setFlagAtivo(false);
        usuario.setRoleString("USUARIO");

        // senha criptografada
        usuario.setSenha(usuarioService.encodePassword(dto.getSenha()));

        Usuario salvo = usuarioService.salvar(usuario);

        PessoaResponseDTO resp = modelMapper.map(salvo, PessoaResponseDTO.class);
        return ResponseEntity.ok(resp);
    }

    // ============================================================
    // 📋 LISTAR — FUNCIONARIO ou ADMIN
    // ============================================================
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<List<PessoaResponseDTO>> listar() {

        List<PessoaResponseDTO> pessoas = usuarioService.listarUsuarios()
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
