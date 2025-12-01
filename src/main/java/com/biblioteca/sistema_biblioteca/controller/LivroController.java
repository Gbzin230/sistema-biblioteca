package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.*;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.service.LivroService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;
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

        Page<LivroResponseDTO> dtoPage = livrosPage.map(livro -> {
            LivroResponseDTO dto = new LivroResponseDTO();

            dto.setId(livro.getId());
            dto.setTitulo(livro.getTitulo());
            dto.setAutor(livro.getAutor());
            dto.setEditora(livro.getEditora());
            dto.setTema(livro.getTema());
            dto.setObra(livro.getObra());

            dto.setAutores(livro.getAutoresLista());
            dto.setTemas(livro.getTemasLista());
            dto.setTags(livro.getTags());

            dto.setAnoLancamento(livro.getAnoLancamento());
            dto.setQuantidadeDisponivel(livro.getQuantidadeDisponivel());
            dto.setQuantidadeDisponivelEmprestar(livro.getQuantidadeDisponivelEmprestar());
            dto.setSinopse(livro.getSinopse());
            dto.setStatus(livro.getStatusNome());
            dto.setFlagAtivo(livro.getFlagAtivo());
            dto.setDtValidade(livro.getDtValidade());

            dto.setUriImgLivro(livro.getUriImgLivro());
            dto.setUrlLivro(livro.getUriArquivoLivro());

            return dto;
        });

        ApiPageResponse<LivroResponseDTO> response = ApiPageResponse.of(dtoPage, dto -> dto);

        return ResponseEntity.ok(new ApiResponse<>(response, "Livros listados com sucesso"));
    }

    // ✅ BUSCAR POR ID — público
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LivroResponseDTO>> buscarPorId(@PathVariable Long id) {
        Livro livro = livroService.buscarPorId(id);
        livroService.preencherDisponibilidade(livro);

        LivroResponseDTO dto = new LivroResponseDTO();

        dto.setId(livro.getId());
        dto.setTitulo(livro.getTitulo());
        dto.setAutor(livro.getAutor());
        dto.setEditora(livro.getEditora());
        dto.setTema(livro.getTema());
        dto.setObra(livro.getObra());

        dto.setAutores(livro.getAutoresLista());
        dto.setTemas(livro.getTemasLista());
        dto.setTags(livro.getTags());

        dto.setAnoLancamento(livro.getAnoLancamento());
        dto.setQuantidadeDisponivel(livro.getQuantidadeDisponivel());
        dto.setQuantidadeDisponivelEmprestar(livro.getQuantidadeDisponivelEmprestar());
        dto.setSinopse(livro.getSinopse());
        dto.setStatus(livro.getStatusNome());
        dto.setFlagAtivo(livro.getFlagAtivo());
        dto.setDtValidade(livro.getDtValidade());

        dto.setUriImgLivro(livro.getUriImgLivro());
        dto.setUrlLivro(livro.getUriArquivoLivro());

        return ResponseEntity.ok(new ApiResponse<>(dto, "Livro encontrado com sucesso"));
    }

    // ✅ LISTAR LIVROS POR TEMA — AGORA SOMENTE ATIVOS
    @GetMapping("/tema/{tema}")
    public ResponseEntity<ApiResponse<List<LivroResponseDTO>>> listarPorTema(@PathVariable String tema) {

        // 🔥 AGORA só retorna livros ativos
        List<Livro> livros = livroService.buscarPorTemaApenasAtivos(tema);

        List<LivroResponseDTO> dtos = livros.stream().map(livro -> {
            LivroResponseDTO dto = new LivroResponseDTO();

            dto.setId(livro.getId());
            dto.setTitulo(livro.getTitulo());
            dto.setAutor(livro.getAutor());
            dto.setEditora(livro.getEditora());
            dto.setTema(livro.getTema());
            dto.setObra(livro.getObra());

            dto.setAutores(livro.getAutoresLista());
            dto.setTemas(livro.getTemasLista());
            dto.setTags(livro.getTags());

            dto.setAnoLancamento(livro.getAnoLancamento());
            dto.setQuantidadeDisponivel(livro.getQuantidadeDisponivel());
            dto.setQuantidadeDisponivelEmprestar(livro.getQuantidadeDisponivelEmprestar());
            dto.setSinopse(livro.getSinopse());
            dto.setStatus(livro.getStatusNome());
            dto.setFlagAtivo(livro.getFlagAtivo());
            dto.setDtValidade(livro.getDtValidade());

            dto.setUriImgLivro(livro.getUriImgLivro());
            dto.setUrlLivro(livro.getUriArquivoLivro());

            return dto;
        }).toList();

        return ResponseEntity.ok(
                new ApiResponse<>(dtos, "Livros ativos do tema '" + tema + "' listados com sucesso"));
    }

    // ✅ LISTAR APENAS LIVROS ATIVOS — público
    @GetMapping("/ativos")
    public ResponseEntity<ApiResponse<ApiPageResponse<LivroResponseDTO>>> listarAtivos(Pageable pageable) {

        Page<Livro> livrosPage = livroService.listarAtivos(pageable);

        Page<LivroResponseDTO> dtoPage = livrosPage.map(livro -> {
            LivroResponseDTO dto = new LivroResponseDTO();

            dto.setId(livro.getId());
            dto.setTitulo(livro.getTitulo());
            dto.setAutor(livro.getAutor());
            dto.setEditora(livro.getEditora());
            dto.setTema(livro.getTema());
            dto.setObra(livro.getObra());

            dto.setAutores(livro.getAutoresLista());
            dto.setTemas(livro.getTemasLista());
            dto.setTags(livro.getTags());

            dto.setAnoLancamento(livro.getAnoLancamento());
            dto.setQuantidadeDisponivel(livro.getQuantidadeDisponivel());
            dto.setQuantidadeDisponivelEmprestar(livro.getQuantidadeDisponivelEmprestar());
            dto.setSinopse(livro.getSinopse());
            dto.setStatus(livro.getStatusNome());
            dto.setFlagAtivo(livro.getFlagAtivo());
            dto.setDtValidade(livro.getDtValidade());

            dto.setUriImgLivro(livro.getUriImgLivro());
            dto.setUrlLivro(livro.getUriArquivoLivro());

            return dto;
        });

        ApiPageResponse<LivroResponseDTO> response = ApiPageResponse.of(dtoPage, dto -> dto);

        return ResponseEntity.ok(new ApiResponse<>(response, "Livros ativos listados com sucesso"));
    }

    //listar com Busca
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ApiPageResponse<LivroResponseDTO>>> buscarGlobal(
            @RequestParam String q,
            Pageable pageable
    ) {
        Page<Livro> livrosPage = livroService.buscarGlobal(q, pageable);

        Page<LivroResponseDTO> dtoPage = livrosPage.map(livro -> {
            LivroResponseDTO dto = new LivroResponseDTO();

            dto.setId(livro.getId());
            dto.setTitulo(livro.getTitulo());
            dto.setAutor(livro.getAutor());
            dto.setEditora(livro.getEditora());
            dto.setTema(livro.getTema());
            dto.setObra(livro.getObra());
            dto.setAnoLancamento(livro.getAnoLancamento());
            dto.setQuantidadeDisponivel(livro.getQuantidadeDisponivel());
            dto.setQuantidadeDisponivelEmprestar(livro.getQuantidadeDisponivelEmprestar());
            dto.setTemas(livro.getTemasLista());
            dto.setAutores(livro.getAutoresLista());
            dto.setTags(livro.getTags());
            dto.setUriImgLivro(livro.getUriImgLivro());
            dto.setUrlLivro(livro.getUriArquivoLivro());
            dto.setStatus(livro.getStatusNome());
            dto.setFlagAtivo(livro.getFlagAtivo());
            dto.setDtValidade(livro.getDtValidade());

            return dto;
        });

        ApiPageResponse<LivroResponseDTO> response = ApiPageResponse.of(dtoPage, dto -> dto);
        return ResponseEntity.ok(new ApiResponse<>(response, "Resultado da busca global"));
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

    // 🚫 UPLOAD COM ARQUIVOS
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LivroRequestDTO.class)))
    public ResponseEntity<?> criarComUpload(
            @RequestPart(value = "dto") @Valid LivroRequestDTO dto,
            @RequestPart(value = "capa", required = false) MultipartFile capa,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf) {

        Livro salvo = livroService.criarComArquivos(dto, capa, pdf);

        LivroResponseDTO response = modelMapper.map(salvo, LivroResponseDTO.class);
        return ResponseEntity.ok(new ApiResponse<>(response, "Livro criado com sucesso"));
    }

    // 🚫 Atualizar livro
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LivroResponseDTO>> atualizar(@PathVariable Long id,
            @Valid @RequestBody LivroRequestDTO dto) {
        Livro atualizado = livroService.atualizar(id, modelMapper.map(dto, Livro.class));
        LivroResponseDTO response = modelMapper.map(atualizado, LivroResponseDTO.class);
        return ResponseEntity.ok(new ApiResponse<>(response, "Livro atualizado com sucesso"));
    }

    // 🚫 Deletar livro
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletar(@PathVariable Long id) {
        livroService.deletar(id);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Livro removido com sucesso"));
    }

    // 🚫 DESATIVAR EM MASSA
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @PostMapping("/desativar-multiplos")
    public ResponseEntity<ApiResponse<String>> desativarMultiplos(@RequestBody List<Long> ids) {

        int total = livroService.desativarLivrosEmMassa(ids);

        return ResponseEntity.ok(
                new ApiResponse<>("OK", total + " livro(s) desativado(s) com sucesso"));
    }

}
