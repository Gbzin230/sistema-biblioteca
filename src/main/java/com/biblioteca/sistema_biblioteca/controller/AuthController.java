package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.security.JwtUtil;
import com.biblioteca.sistema_biblioteca.dto.LoginDTO;
import com.biblioteca.sistema_biblioteca.service.CustomUserDetailsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {

        // 1 — busca usuário
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(loginDTO.getUsername());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Usuário não encontrado.",
                    "status", 403
            ));
        }

        // 2 — pega entidade Pessoa
        Pessoa pessoa = userDetailsService
                .getPessoaByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        // 3 — verifica aprovação
        if (!pessoa.isFlagAtivo()) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Usuário ainda não aprovado.",
                    "aguardandoAprovacao", true,
                    "status", 403
            ));
        }

        // 4 — autentica
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getSenha()
                )
        );

        // 5 — gera token
        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        String token = jwtUtil.generateToken(loginDTO.getUsername(), role);

        return ResponseEntity.ok(Map.of("token", "Bearer " + token));
    }
}
