package com.biblioteca.sistema_biblioteca;

import com.biblioteca.sistema_biblioteca.model.Admin;
import com.biblioteca.sistema_biblioteca.model.Funcionario;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;

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
CommandLineRunner init(UsuarioRepository usuarioRepo, 
                       LivroRepository livroRepository, 
                       PasswordEncoder encoder) {
    return args -> {

        // ENCODE DE SENHAS APENAS PARA ENTIDADES USUARIO (INCLUINDO ADMIN/FUNCIONARIO)
        usuarioRepo.findAll().forEach(usuario -> {
            if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
                usuario.setSenha(encoder.encode(usuario.getSenha()));
                usuarioRepo.save(usuario);
            }
        });

        // Aqui você pode inserir livros de teste se quiser.
    };
}
}

