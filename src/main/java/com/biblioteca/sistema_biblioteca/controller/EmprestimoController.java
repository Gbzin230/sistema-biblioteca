package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.EmprestimoRequestDTO;
import com.biblioteca.sistema_biblioteca.dto.EmprestimoResponseDTO;
import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.service.EmprestimoService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    // ============================================================
    // 1. REALIZAR EMPRÉSTIMO
    // ============================================================
    @PostMapping
    public ResponseEntity<EmprestimoResponseDTO> realizarEmprestimo(
            @Valid @RequestBody EmprestimoRequestDTO request,
            Authentication auth) {

        String authUsername = auth.getName();

        // Usuário só pode emprestar em nome dele mesmo
        emprestimoService.validarUsuarioEmprestimo(request.getUsername(), authUsername);

        Emprestimo emprestimo = emprestimoService.realizarEmprestimo(
                request.getUsername(),
                request.getLivroId()
        );

        return ResponseEntity.ok(toDTO(emprestimo));
    }

    // ============================================================
    // 2. RENOVAR EMPRÉSTIMO
    // ============================================================
    @PutMapping("/{id}/renovar")
    public ResponseEntity<EmprestimoResponseDTO> renovarEmprestimo(
            @PathVariable Long id,
            Authentication auth) {

        String username = auth.getName();

        // Garante que o cara só renova o próprio empréstimo
        emprestimoService.validarDonoDoEmprestimo(id, username);

        Emprestimo renovado = emprestimoService.renovarEmprestimo(id);

        return ResponseEntity.ok(toDTO(renovado));
    }

    // ============================================================
    // 3. DEVOLVER LIVRO
    // ============================================================
    @PutMapping("/{id}/devolver")
    public ResponseEntity<Void> devolverLivro(
            @PathVariable Long id,
            Authentication auth) {

        String username = auth.getName();

        // Librarian/Admin pode devolver tudo. Usuário só o próprio
        emprestimoService.devolverAutorizado(id, username);

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // 4. BUSCAR POR ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoResponseDTO> buscarPorId(
            @PathVariable Long id,
            Authentication auth) {

        String username = auth.getName();

        emprestimoService.validarDonoDoEmprestimo(id, username);

        Emprestimo emp = emprestimoService.buscarPorId(id);

        return ResponseEntity.ok(toDTO(emp));
    }

    // ============================================================
    // 5. LISTAR TODOS (somente ADMIN/BIBLIOTECARIO)
    // ============================================================
    @GetMapping
    public ResponseEntity<List<EmprestimoResponseDTO>> listarTodos() {

        List<EmprestimoResponseDTO> lista = emprestimoService.listarEmprestimos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ============================================================
    // 6. LISTAR ATRASADOS (somente STAFF)
    // ============================================================
    @GetMapping("/atrasados")
    public ResponseEntity<List<EmprestimoResponseDTO>> listarAtrasados() {

        List<EmprestimoResponseDTO> lista = emprestimoService.buscarEmprestimosAtrasados()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ============================================================
    // CONVERSOR PARA DTO — corrigido com timezone
    // ============================================================
    private EmprestimoResponseDTO toDTO(Emprestimo e) {
        EmprestimoResponseDTO dto = new EmprestimoResponseDTO();

        dto.setId(e.getId());
        dto.setUsuarioId(e.getUsuario().getUsername());
        dto.setLivroId(e.getLivro().getId());

        // 🔥 Conversão correta preservando data real do Brasil
        dto.setDtInicio(
                e.getDtInicio().atZone(ZONE).toLocalDate()
        );

        dto.setDtPrevistaDevolucao(
                e.getDtFim().atZone(ZONE).toLocalDate()
        );

        dto.setNumRenovacoes(e.getNumRenovacoes());
        dto.setStatus(e.getStatus().getNome());

        return dto;
    }
}
