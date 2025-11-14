package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.model.Usuario;
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

    public FuncionarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 🔥 AGORA USA username, NÃO Long id
    @PutMapping("/aprovar-usuario/{username}")
    public ResponseEntity<String> aprovarUsuario(@PathVariable String username) {
        Usuario aprovado = usuarioService.aprovarUsuario(username);
        return ResponseEntity.ok("Usuário '" + aprovado.getNome() + "' aprovado com sucesso!");
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }
}
