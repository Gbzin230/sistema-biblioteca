package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.*;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.service.EmprestimoScheduler;
import com.biblioteca.sistema_biblioteca.service.EmprestimoService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private final EmprestimoScheduler emprestimoScheduler;
    private final ModelMapper modelMapper;

    public EmprestimoController(EmprestimoService emprestimoService,
                                EmprestimoScheduler emprestimoScheduler,
                                ModelMapper modelMapper) {
        this.emprestimoService = emprestimoService;
        this.emprestimoScheduler = emprestimoScheduler;
        this.modelMapper = modelMapper;
    }

    // 🎯 Criar empréstimo — USUÁRIO autenticado
    @PreAuthorize("hasRole('USUARIO')")
    @PostMapping
    public ResponseEntity<ApiResponse<EmprestimoResponseDTO>> criar(@Valid @RequestBody EmprestimoRequestDTO dto,
                                                                    Authentication auth) {
        // ✅ checa se o usuário autenticado é o mesmo do empréstimo
        String username = auth.getName();
        emprestimoService.validarUsuarioEmprestimo(dto.getUsuarioId(), username);

        Emprestimo criado = emprestimoService.realizarEmprestimo(dto.getUsuarioId(), dto.getLivroId());
        EmprestimoResponseDTO resp = modelMapper.map(criado, EmprestimoResponseDTO.class);
        if (criado.getUsuario() != null) resp.setUsuarioId(criado.getUsuario().getId());
        if (criado.getLivro() != null) resp.setLivroId(criado.getLivro().getId());
        return ResponseEntity.ok(new ApiResponse<>(resp, "Empréstimo realizado com sucesso."));
    }

    // 🎯 Renovar empréstimo — somente o dono
    @PreAuthorize("hasRole('USUARIO')")
    @PutMapping("/{id}/renovar")
    public ResponseEntity<ApiResponse<EmprestimoResponseDTO>> renovar(@PathVariable Long id, Authentication auth) {
        emprestimoService.validarDonoDoEmprestimo(id, auth.getName());
        Emprestimo renovado = emprestimoService.renovarEmprestimo(id);
        EmprestimoResponseDTO resp = modelMapper.map(renovado, EmprestimoResponseDTO.class);
        if (renovado.getUsuario() != null) resp.setUsuarioId(renovado.getUsuario().getId());
        if (renovado.getLivro() != null) resp.setLivroId(renovado.getLivro().getId());
        return ResponseEntity.ok(new ApiResponse<>(resp, "Empréstimo renovado com sucesso."));
    }

    // ✅ Devolver — USUÁRIO (próprio) ou ADMIN/FUNCIONARIO (forçado)
    @PreAuthorize("hasAnyRole('USUARIO','FUNCIONARIO','ADMIN')")
    @PutMapping("/{id}/devolver")
    public ResponseEntity<ApiResponse<String>> devolver(@PathVariable Long id, Authentication auth) {
        emprestimoService.devolverAutorizado(id, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("OK", "Livro devolvido com sucesso."));
    }

    // 👀 Listar todos — ADMIN/FUNCIONARIO
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmprestimoResponseDTO>>> listar() {
        List<EmprestimoResponseDTO> lista = emprestimoService.listarEmprestimos().stream()
                .map(e -> {
                    EmprestimoResponseDTO r = modelMapper.map(e, EmprestimoResponseDTO.class);
                    if (e.getUsuario() != null) r.setUsuarioId(e.getUsuario().getId());
                    if (e.getLivro() != null) r.setLivroId(e.getLivro().getId());
                    return r;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(lista, "Lista de empréstimos"));
    }

    // 👀 Atrasados — ADMIN/FUNCIONARIO
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping("/atrasados")
    public ResponseEntity<ApiResponse<List<EmprestimoResponseDTO>>> atrasados() {
        List<EmprestimoResponseDTO> atrasados = emprestimoService.buscarEmprestimosAtrasados().stream()
                .map(e -> {
                    EmprestimoResponseDTO r = modelMapper.map(e, EmprestimoResponseDTO.class);
                    if (e.getUsuario() != null) r.setUsuarioId(e.getUsuario().getId());
                    if (e.getLivro() != null) r.setLivroId(e.getLivro().getId());
                    return r;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(atrasados, "Empréstimos atrasados"));
    }

    // ✅ Status — dono, FUNCIONARIO ou ADMIN
    @PreAuthorize("hasAnyRole('USUARIO','FUNCIONARIO','ADMIN')")
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> status(@PathVariable Long id, Authentication auth) {
        emprestimoService.validarDonoDoEmprestimo(id, auth.getName());
        String status = emprestimoService.buscarPorId(id).verificarStatus().name();
        return ResponseEntity.ok(new ApiResponse<>(status, "Status do empréstimo"));
    }

    // ⚙️ Rotina de devolução automática — ADMIN/FUNCIONARIO
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @PostMapping("/devolver-vencidos")
    public ResponseEntity<ApiResponse<String>> devolverVencidos() {
        emprestimoScheduler.devolverEmprestimosVencidos();
        return ResponseEntity.ok(new ApiResponse<>("Processo de devolução automática executado com sucesso!", "Rotina manual"));
    }
}

