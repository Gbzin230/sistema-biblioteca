package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.PessoaRequestDTO;
import com.biblioteca.sistema_biblioteca.dto.PessoaResponseDTO;
import com.biblioteca.sistema_biblioteca.dto.PessoaUpdateDTO;
import com.biblioteca.sistema_biblioteca.dto.UsuarioListagemDTO;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.model.Role;
import com.biblioteca.sistema_biblioteca.service.UsuarioService;
import com.biblioteca.sistema_biblioteca.service.FuncionarioService;
import com.biblioteca.sistema_biblioteca.repository.RoleRepository;
import com.biblioteca.sistema_biblioteca.repository.StatusUsuarioRepository;

import org.modelmapper.ModelMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final UsuarioService usuarioService;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final StatusUsuarioRepository statusUsuarioRepository;
    private final FuncionarioService funcionarioService;

    // 🔥 CONSTRUTOR ÚNICO
    public PessoaController(
            UsuarioService usuarioService,
            RoleRepository roleRepository,
            ModelMapper modelMapper,
            FuncionarioService funcionarioService,
            StatusUsuarioRepository statusUsuarioRepository
    ) {
        this.usuarioService = usuarioService;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.funcionarioService = funcionarioService;
        this.statusUsuarioRepository = statusUsuarioRepository;
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

        usuario.setLimiteSlots(3);
        usuario.setFlagAtivo(false);

        Role userRole = roleRepository.findByNomeIgnoreCase("USUARIO")
                .orElseThrow(() -> new RuntimeException("Role USUARIO não encontrada"));
        usuario.setRole(userRole);

        usuario.setSenha(usuarioService.encodePassword(dto.getSenha()));

        Usuario salvo = usuarioService.salvar(usuario);

        PessoaResponseDTO resp = modelMapper.map(salvo, PessoaResponseDTO.class);
        return ResponseEntity.ok(resp);
    }

    // ============================================================
    // 📋 LISTAGEM — FUNCIONÁRIO OU ADMIN
    // ============================================================
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping("/usuarios")
    public List<UsuarioListagemDTO> listarUsuarios() {
        return usuarioService.listarUsuariosCompleto();
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

    // ============================================================
    // 📝 ATUALIZAR DADOS
    // ============================================================
    @PutMapping("/update/{identificador}")
    public ResponseEntity<PessoaResponseDTO> atualizar(
            @PathVariable String identificador,
            @RequestBody PessoaUpdateDTO dto
    ) {
        Usuario atualizado = usuarioService.atualizarUsuario(dto, identificador);
        PessoaResponseDTO resp = modelMapper.map(atualizado, PessoaResponseDTO.class);
        return ResponseEntity.ok(resp);
    }
}
