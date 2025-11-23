package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.UsuarioListagemDTO;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.service.FuncionarioService;
import com.biblioteca.sistema_biblioteca.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/funcionarios")
@PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
public class FuncionarioController {

    private final UsuarioService usuarioService;
    private final FuncionarioService funcionarioService;

    // ✅ CONSTRUTOR ÚNICO
    public FuncionarioController(
            UsuarioService usuarioService,
            FuncionarioService funcionarioService
    ) {
        this.usuarioService = usuarioService;
        this.funcionarioService = funcionarioService;
    }

    // 🔥 APROVAR USUÁRIO PELO USERNAME
    @PutMapping("/aprovar-usuario/{username}")
    public ResponseEntity<String> aprovarUsuario(@PathVariable String username) {
        Usuario aprovado = usuarioService.aprovarUsuario(username);
        return ResponseEntity.ok("Usuário '" + aprovado.getNome() + "' aprovado com sucesso!");
    }

    // 🔥 LISTAR FUNCIONARIOS (com DTO)
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public List<UsuarioListagemDTO> listarUsuarios() {
        return funcionarioService.consultarFuncionarios();
    }
}
