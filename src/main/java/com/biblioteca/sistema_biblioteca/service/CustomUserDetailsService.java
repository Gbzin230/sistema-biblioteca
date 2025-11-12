package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PessoaRepository pessoaRepository;

    public CustomUserDetailsService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Pessoa pessoa = pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // 🚫 Bloqueia login se o usuário ainda não foi aprovado
        if (!pessoa.isFlagAtivo()) {
            throw new UsernameNotFoundException("Usuário ainda não aprovado pelo sistema. Aguarde validação de um funcionário ou admin.");
        }

        String role = "ROLE_" + pessoa.getRoleString().toUpperCase();
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        return User.builder()
                .username(pessoa.getUsername())
                .password(pessoa.getSenha())
                .authorities(authorities)
                .build();
    }

    public Optional<Pessoa> getPessoaByUsername(String username) {
        return pessoaRepository.findByUsername(username);
    }
}
