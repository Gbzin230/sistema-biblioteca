package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.service.EmprestimoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping("/{usuarioId}/{livroId}")
    public ResponseEntity<Emprestimo> realizarEmprestimo(@PathVariable Long usuarioId, @PathVariable Long livroId) {
        Emprestimo e = emprestimoService.realizarEmprestimo(usuarioId, livroId);
        return ResponseEntity.status(201).body(e);
    }

    @PutMapping("/{id}/renovar")
    public ResponseEntity<Emprestimo> renovar(@PathVariable Long id) {
        Emprestimo renovado = emprestimoService.renovarEmprestimo(id);
        return ResponseEntity.ok(renovado);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<Void> devolver(@PathVariable Long id) {
        emprestimoService.devolverLivro(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Emprestimo>> listar() {
        return ResponseEntity.ok(emprestimoService.listarEmprestimos());
    }
}
