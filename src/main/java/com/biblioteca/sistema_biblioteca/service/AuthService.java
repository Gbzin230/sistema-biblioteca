package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.dto.LoginDTO;
import com.biblioteca.sistema_biblioteca.exception.UsuarioNaoEncontradoException;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthService {

    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PessoaRepository pessoaRepository, PasswordEncoder passwordEncoder) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Pessoa login(LoginDTO dto) {
        Pessoa pessoa = pessoaRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário ou senha inválidos."));

        if (!passwordEncoder.matches(dto.getSenha(), pessoa.getSenha())) {
            throw new UsuarioNaoEncontradoException("Usuário ou senha inválidos.");
        }

        return pessoa;
    }
}
