package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.LoginDTO;
import com.biblioteca.sistema_biblioteca.dto.PessoaResponseDTO;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.service.AuthService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;

    public AuthController(AuthService authService, ModelMapper modelMapper) {
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<PessoaResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        Usuario usuario = authService.login(dto);
        PessoaResponseDTO response = modelMapper.map(usuario, PessoaResponseDTO.class);
        return ResponseEntity.ok(response);
    }
}
