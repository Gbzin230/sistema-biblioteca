package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PessoaRepository pessoaRepository;

    public CustomUserDetailsService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    // =======================================================
    // 🔎 BUSCAR PESSOA POR USERNAME OU EMAIL
    // =======================================================
    public Optional<Pessoa> getPessoaByUsername(String usernameOrEmail) {

        // 1️⃣ Busca por username
        Optional<Pessoa> usuario = pessoaRepository.findByUsername(usernameOrEmail);
        if (usuario.isPresent()) return usuario;

        // 2️⃣ Busca por e-mail se não encontrou username
        return pessoaRepository.findByEmail(usernameOrEmail);
    }

    // =======================================================
    // 🔐 MÉTODO OBRIGATÓRIO DO SPRING SECURITY
    // =======================================================
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        Pessoa pessoa = getPessoaByUsername(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + usernameOrEmail));

        return new User(
                pessoa.getUsername(),         // 👍 Sempre retorna username correto
                pessoa.getSenha(),            // 👍 Senha hash
                Collections.singleton(() -> "ROLE_" + pessoa.getRoleString()) // 👍 converte para ROLE_...
        );
    }
}
