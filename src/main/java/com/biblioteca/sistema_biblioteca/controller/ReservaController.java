package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.*;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.StatusReserva;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.StatusReservaRepository;
import com.biblioteca.sistema_biblioteca.service.ReservaService;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final StatusReservaRepository statusReservaRepository;
    private final ModelMapper modelMapper;

    public ReservaController(
            ReservaService reservaService,
            UsuarioRepository usuarioRepository,
            LivroRepository livroRepository,
            StatusReservaRepository statusReservaRepository,
            ModelMapper modelMapper
    ) {
        this.reservaService = reservaService;
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.statusReservaRepository = statusReservaRepository;
        this.modelMapper = modelMapper;
    }

    // ============================================================
    // 🎯 Criar reserva — somente o próprio USUÁRIO autenticado
    // ============================================================
    @PreAuthorize("hasRole('USUARIO')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponseDTO>> criar(
            @Valid @RequestBody ReservaRequestDTO dto,
            Authentication auth) {

        String usernameLogado = auth.getName();

        // valida se é o mesmo usuário
        reservaService.validarUsuarioReserva(dto.getUsername(), usernameLogado);

        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Livro livro = livroRepository.findById(dto.getLivroId())
                .orElseThrow(() -> new RuntimeException("Livro não encontrado."));

        // 🔥 PEGAR STATUS 'ATIVA'
        StatusReserva statusAtiva = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RuntimeException("Status 'ATIVA' não existe."));

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLivro(livro);
        reserva.setStatus(statusAtiva);

        Reserva salva = reservaService.criarReserva(reserva);

        ReservaResponseDTO resp = modelMapper.map(salva, ReservaResponseDTO.class);
        resp.setUsuarioId(usuario.getUsername());
        resp.setLivroId(livro.getId());

        String status = salva.getStatus().getNome().toUpperCase();

        String mensagem = status.equals("CONFIRMADA")
                ? "Livro disponível; empréstimo realizado."
                : "Reserva criada com sucesso.";

        return ResponseEntity.ok(new ApiResponse<>(resp, mensagem));
    }

    // ============================================================
    // ❌ Cancelar reserva — USUÁRIO (se dono) ou ADMIN
    // ============================================================
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<String>> cancelar(
            @PathVariable Long id,
            Authentication auth) {

        reservaService.cancelarAutorizado(id, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("OK", "Reserva cancelada com sucesso."));
    }

    // ============================================================
    // 📋 Listar reservas — FUNCIONARIO / ADMIN
    // ============================================================
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservaResponseDTO>>> listar() {

        List<ReservaResponseDTO> lista = reservaService.listar().stream()
                .map(r -> {
                    ReservaResponseDTO dto = modelMapper.map(r, ReservaResponseDTO.class);
                    if (r.getUsuario() != null)
                        dto.setUsuarioId(r.getUsuario().getUsername());
                    if (r.getLivro() != null)
                        dto.setLivroId(r.getLivro().getId());
                    dto.setStatus(r.getStatus().getNome());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(lista, "Lista de reservas"));
    }

    // ============================================================
    // 📚  HISTÓRICO DE RESERVAS POR USUÁRIO (USUÁRIO / ADMIN / FUNCIONÁRIO)
    // ============================================================
    @PreAuthorize("hasAnyRole('USUARIO','FUNCIONARIO','ADMIN')")
    @GetMapping("/historico/{username}")
    public ResponseEntity<ApiResponse<List<ReservaResponseDTO>>> historicoReservas(
            @PathVariable String username,
            Authentication auth) {

        String authUsername = auth.getName();

        List<ReservaResponseDTO> lista = reservaService.buscarHistoricoReservas(
                username,
                authUsername
        ).stream().map(r -> {
            ReservaResponseDTO dto = modelMapper.map(r, ReservaResponseDTO.class);
            dto.setUsuarioId(r.getUsuario().getUsername());
            dto.setLivroId(r.getLivro().getId());
            dto.setStatus(r.getStatus().getNome());
            dto.setUriImgLivro(r.getLivro().getUriImgLivro());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(lista, "Histórico de reservas"));
    }

}
