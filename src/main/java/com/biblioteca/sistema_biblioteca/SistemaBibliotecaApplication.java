package com.biblioteca.sistema_biblioteca;

import com.biblioteca.sistema_biblioteca.model.Admin;
import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SistemaBibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaBibliotecaApplication.class, args);
    }

    @Bean
    CommandLineRunner init(PessoaRepository repo, LivroRepository livroRepository, PasswordEncoder encoder) {
        return args -> {
            repo.findAll().forEach(pessoa -> {
                if (pessoa.getSenha() != null && !pessoa.getSenha().startsWith("$2a$")) {
                    pessoa.setSenha(encoder.encode(pessoa.getSenha()));
                    repo.save(pessoa);
                }
            });
        };
    }
}

