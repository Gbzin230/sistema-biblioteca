package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.UsuarioListagemDTO;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.service.FuncionarioService;
import com.biblioteca.sistema_biblioteca.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/funcionarios")
@PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
public class FuncionarioController {

    private final UsuarioService usuarioService;
    private final FuncionarioService funcionarioService;

    public FuncionarioController(
            UsuarioService usuarioService,
            FuncionarioService funcionarioService
    ) {
        this.usuarioService = usuarioService;
        this.funcionarioService = funcionarioService;
    }

    @PutMapping("/aprovar-usuario/{username}")
    public ResponseEntity<Map<String, String>> aprovarUsuario(@PathVariable String username) {
        Usuario aprovado = usuarioService.aprovarUsuario(username);
        return ResponseEntity.ok(
            Map.of("mensagem", "Usuário '" + aprovado.getNome() + "' aprovado com sucesso!")
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    public List<UsuarioListagemDTO> listarUsuarios() {
        return funcionarioService.consultarFuncionarios();
    }

    @PostMapping("/aprovar-multiplos")
    @PreAuthorize("hasAnyRole('ADMIN','FUNCIONARIO')")
    public ResponseEntity<?> aprovarMultiplos(@RequestBody Map<String, List<String>> body) {

        List<String> usernames = body.get("usernames");

        int total = funcionarioService.aprovarFuncionariosEmMassa(usernames);

        return ResponseEntity.ok(
            Map.of(
                "message", "Usuários aprovados com sucesso",
                "total", total
            )
        );
    }

    @PostMapping("/recusar-multiplos")
    @PreAuthorize("hasAnyRole('ADMIN','FUNCIONARIO')")
    public ResponseEntity<?> recusarMultiplos(@RequestBody Map<String, List<String>> body) {

        List<String> usernames = body.get("usernames");

        int total = funcionarioService.recusarFuncionariosEmMassa(usernames);

        return ResponseEntity.ok(
            Map.of(
                "message", "Usuários recusados com sucesso",
                "total", total
            )
        );
    }

}
