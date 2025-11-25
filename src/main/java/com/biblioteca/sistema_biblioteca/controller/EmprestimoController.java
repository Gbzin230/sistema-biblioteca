package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.EmprestimoRequestDTO;
import com.biblioteca.sistema_biblioteca.dto.EmprestimoResponseDTO;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.service.EmprestimoService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    // 5. LISTAR TODOS (ADMIN / FUNCIONARIO)
    // ============================================================
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<List<EmprestimoResponseDTO>> listarTodos() {

        List<EmprestimoResponseDTO> lista = emprestimoService.listarEmprestimos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ============================================================
    // 7. NOVO: LISTAR EMPRÉSTIMOS POR USUÁRIO
    // ============================================================
    @GetMapping("/usuario/{username}")
    public ResponseEntity<List<EmprestimoResponseDTO>> listarPorUsuario(
            @PathVariable String username,
            Authentication auth) {

        String authUser = auth.getName();
        boolean isAdminOrFuncionario = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN") ||
                               r.getAuthority().equals("ROLE_FUNCIONARIO"));

        List<EmprestimoResponseDTO> lista =
                emprestimoService.consultarEmprestimosUsuario(
                        username,
                        authUser,
                        isAdminOrFuncionario
                ).stream().map(this::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ============================================================
    // 7. HISTÓRICO DE EMPRÉSTIMOS POR USERNAME
    // ============================================================
    @GetMapping("/historico/{username}")
    public ResponseEntity<List<EmprestimoResponseDTO>> historico(
            @PathVariable String username,
            Authentication auth) {

        String authUsername = auth.getName();

        List<EmprestimoResponseDTO> lista = emprestimoService.buscarHistorico(username, authUsername)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }


    // ============================================================
    // CONVERSOR PARA DTO
    // ============================================================
    private EmprestimoResponseDTO toDTO(Emprestimo e) {
        EmprestimoResponseDTO dto = new EmprestimoResponseDTO();

        dto.setId(e.getId());
        dto.setUsuarioId(e.getUsuario().getUsername());
        dto.setLivroId(e.getLivro().getId());

        dto.setTituloLivro(e.getLivro().getTitulo());
        dto.setUriImgLivro(e.getLivro().getUriImgLivro());

        dto.setDtInicio(e.getDtInicio().atZone(ZONE).toLocalDate());
        dto.setDtPrevistaDevolucao(e.getDtFim().atZone(ZONE).toLocalDate());

        dto.setNumRenovacoes(e.getNumRenovacoes());
        dto.setStatus(e.getStatus().getNome());

        return dto;
    }
}
