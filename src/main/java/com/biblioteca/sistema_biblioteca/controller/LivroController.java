package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.*;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.service.LivroService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService livroService;
    private final LivroRepository livroRepository;
    private final ModelMapper modelMapper;

    public LivroController(LivroService livroService, LivroRepository livroRepository, ModelMapper modelMapper) {
        this.livroService = livroService;
        this.livroRepository = livroRepository;
        this.modelMapper = modelMapper;
    }
    // ✅ LISTAR LIVROS — público
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<LivroResponseDTO>>> listar(Pageable pageable) {
        Page<Livro> livrosPage = livroRepository.findAll(pageable);
        var response = ApiPageResponse.of(livrosPage, livro -> modelMapper.map(livro, LivroResponseDTO.class));
        return ResponseEntity.ok(new ApiResponse<>(response, "Livros listados com sucesso"));
    }

    // ✅ BUSCAR POR ID — público
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LivroResponseDTO>> buscarPorId(@PathVariable Long id) {
        Livro livro = livroService.buscarPorId(id);
        LivroResponseDTO responseDTO = modelMapper.map(livro, LivroResponseDTO.class);
        return ResponseEntity.ok(new ApiResponse<>(responseDTO, "Livro encontrado com sucesso"));
    }

    // 🚫 Criar livro — SOMENTE FUNCIONÁRIO OU ADMIN
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<LivroResponseDTO>> criar(@Valid @RequestBody LivroRequestDTO dto) {
        Livro livro = modelMapper.map(dto, Livro.class);
        Livro salvo = livroService.salvar(livro);
        LivroResponseDTO response = modelMapper.map(salvo, LivroResponseDTO.class);
        return ResponseEntity.ok(new ApiResponse<>(response, "Livro criado com sucesso"));
    }

    // 🚫 Atualizar livro — SOMENTE FUNCIONÁRIO OU ADMIN
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LivroResponseDTO>> atualizar(@PathVariable Long id,
                                                                   @Valid @RequestBody LivroRequestDTO dto) {
        Livro atualizado = livroService.atualizar(id, modelMapper.map(dto, Livro.class));
        LivroResponseDTO response = modelMapper.map(atualizado, LivroResponseDTO.class);
        return ResponseEntity.ok(new ApiResponse<>(response, "Livro atualizado com sucesso"));
    }

    // 🚫 Deletar livro — SOMENTE ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletar(@PathVariable Long id) {
        livroService.deletar(id);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Livro removido com sucesso"));
    }
}


