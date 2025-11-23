package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.dto.LoginDTO;
import com.biblioteca.sistema_biblioteca.exception.UsuarioNaoEncontradoException;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario login(LoginDTO dto) {

        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException("Usuário ou senha inválidos."));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new UsuarioNaoEncontradoException("Usuário ou senha inválidos.");
        }

        return usuario;
    }
}
