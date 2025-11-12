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
        // 1. Busca o usuário
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(loginDTO.getUsername());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(403).body(Map.of("erro", "Usuário não encontrado ou não aprovado"));
        }

        // 2. Se chegou até aqui, ele existe — mas vamos confirmar se está ativo
        Pessoa pessoa = userDetailsService
                .getPessoaByUsername(loginDTO.getUsername()) // cria esse método no service
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        if (!pessoa.isFlagAtivo()) {
            return ResponseEntity.status(403).body(Map.of("erro", "Usuário ainda não aprovado pelo sistema."));
        }

        // 3. Agora autentica normalmente
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getSenha())
        );

        // 4. Gera o token JWT
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtUtil.generateToken(loginDTO.getUsername(), role);

        return ResponseEntity.ok(Map.of("token", "Bearer " + token));
    }

}
