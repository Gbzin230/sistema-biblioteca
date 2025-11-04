package com.biblioteca.sistema_biblioteca.controller;

import java.util.List;

import com.biblioteca.sistema_biblioteca.dto.ApiPageResponse;
import com.biblioteca.sistema_biblioteca.dto.LivroResponseDTO;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.service.LivroService;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService livroService;
    private final ModelMapper modelMapper;
    private final LivroRepository livroRepository;

    public LivroController(LivroService livroService, ModelMapper modelMapper, LivroRepository livroRepository) {
        this.livroService = livroService;
        this.modelMapper = modelMapper;
        this.livroRepository = livroRepository;
    }

    @PostMapping
    public Livro criarLivro(@RequestBody Livro livro) {
        return livroService.salvarLivro(livro);
    }

    @GetMapping
    public ResponseEntity<Page<Livro>> listarLivros(
            @PageableDefault(size = 5, sort = "titulo") Pageable pageable,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor) {

        Page<Livro> livros;

        if (titulo != null && autor != null) {
            livros = livroRepository.findByTituloContainingIgnoreCaseAndAutorContainingIgnoreCase(titulo, autor, pageable);
        } else if (titulo != null) {
            livros = livroRepository.findByTituloContainingIgnoreCase(titulo, pageable);
        } else if (autor != null) {
            livros = livroRepository.findByAutorContainingIgnoreCase(autor, pageable);
        } else {
            livros = livroRepository.findAll(pageable);
        }

        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarLivro(@PathVariable Long id) {
        return livroService.buscaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(@PathVariable Long id, @RequestBody Livro livroAtualizado) {
        return livroService.atualizarLivro(id, livroAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable Long id) {
        if (livroService.buscaPorId(id).isPresent()) {
            livroService.deletarLivro(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();

    }
}
