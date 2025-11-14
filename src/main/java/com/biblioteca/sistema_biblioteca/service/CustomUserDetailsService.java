package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PessoaRepository pessoaRepository;

    public CustomUserDetailsService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        Pessoa pessoa;

        // 🔍 Login via EMAIL
        if (login.contains("@")) {
            pessoa = pessoaRepository.findByEmail(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Email não encontrado: " + login));
        }
        // 🔍 Login via USERNAME
        else {
            pessoa = pessoaRepository.findByUsername(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
        }

        // 🔒 Não deixa fazer login se não estiver ativo
        if (!pessoa.isFlagAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo ou aguardando aprovação.");
        }

        return User.builder()
                .username(pessoa.getUsername())   // sempre devolve o username real
                .password(pessoa.getSenha())      // senha_hash (BCrypt)
                .roles(pessoa.getRoleString())    // USUARIO, ADMIN, FUNCIONARIO
                .build();
    }
}
