package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.*;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.service.ReservaService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final ModelMapper modelMapper;

    public ReservaController(ReservaService reservaService,
                             UsuarioRepository usuarioRepository,
                             LivroRepository livroRepository,
                             ModelMapper modelMapper) {
        this.reservaService = reservaService;
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.modelMapper = modelMapper;
    }

    // 🎯 Criar reserva — USUÁRIO autenticado
    @PreAuthorize("hasRole('USUARIO')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponseDTO>> criar(@Valid @RequestBody ReservaRequestDTO dto,
                                                                 Authentication auth) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Livro livro = livroRepository.findById(dto.getLivroId())
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        String username = auth.getName();
        reservaService.validarUsuarioReserva(dto.getUsuarioId(), username);
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLivro(livro);
        reserva.setStatus(Reserva.ReservaStatus.ATIVA);

        Reserva salva = reservaService.criarReserva(reserva);
        ReservaResponseDTO resp = modelMapper.map(salva, ReservaResponseDTO.class);
        resp.setUsuarioId(usuario.getId());
        resp.setLivroId(livro.getId());

        String mensagem = (salva.getStatus() == Reserva.ReservaStatus.CONFIRMADA)
                ? "Livro disponível; empréstimo realizado"
                : "Reserva criada com sucesso.";
        return ResponseEntity.ok(new ApiResponse<>(resp, mensagem));
    }

    // ✅ Cancelar reserva — USUÁRIO (sua) ou ADMIN
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<String>> cancelar(@PathVariable Long id, Authentication auth) {
        reservaService.cancelarAutorizado(id, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("OK", "Reserva cancelada com sucesso"));
    }

    // 👀 Listar reservas — FUNCIONARIO / ADMIN
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservaResponseDTO>>> listar() {
        List<ReservaResponseDTO> lista = reservaService.listar().stream()
                .map(r -> {
                    ReservaResponseDTO dto = modelMapper.map(r, ReservaResponseDTO.class);
                    if (r.getUsuario() != null) dto.setUsuarioId(r.getUsuario().getId());
                    if (r.getLivro() != null) dto.setLivroId(r.getLivro().getId());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(lista, "Lista de reservas"));
    }
}

