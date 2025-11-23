package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.ApiResponse;
import com.biblioteca.sistema_biblioteca.service.MetaDadosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metadados")
public class MetaDadosController {

    private final MetaDadosService metaDadosService;

    public MetaDadosController(MetaDadosService metaDadosService) {
        this.metaDadosService = metaDadosService;
    }

    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> listarMetadados() {
        var dados = metaDadosService.buscarTodos();
        return ResponseEntity.ok(new ApiResponse<>(dados, "Metadados carregados com sucesso"));
    }
}
