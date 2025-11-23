package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.security.JwtUtil;
import com.biblioteca.sistema_biblioteca.dto.LoginDTO;
import com.biblioteca.sistema_biblioteca.service.CustomUserDetailsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        // 1 — Carrega credenciais
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(loginDTO.getUsername());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Usuário não encontrado.",
                    "status", 403
            ));
        }

        // 2 — Busca o USUÁRIO (não mais Pessoa!)
        Usuario usuario = userDetailsService.getUsuario(loginDTO.getUsername());

        if (usuario == null) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Usuário não encontrado.",
                    "status", 403
            ));
        }

        // 3 — Verifica BLOQUEIO / INATIVO primeiro
        if (usuario.getCodStatus() != null && (usuario.getCodStatus() == 3 || usuario.getCodStatus() == 4)) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Usuário bloqueado.",
                    "banned", true,
                    "status", 403
            ));
        }

        // 4 — Verifica pendência (aguardando aprovação)
        if (!Boolean.TRUE.equals(usuario.getFlagAtivo()) || usuario.getCodStatus() == null || usuario.getCodStatus() == 1) {
            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Usuário ainda não aprovado.",
                    "aguardandoAprovacao", true,
                    "status", 403
            ));
        }


        // 4 — Autentica usuário
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getSenha()
                )
        );

        // 5 — Extrai role
        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        // 6 — Gera token
        String token = jwtUtil.generateToken(loginDTO.getUsername(), role);

        return ResponseEntity.ok(Map.of("token", "Bearer " + token));
    }
}
