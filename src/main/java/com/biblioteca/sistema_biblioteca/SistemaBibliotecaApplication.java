package com.biblioteca.sistema_biblioteca;

import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // ✅ Habilita agendamentos automáticos no Spring
public class SistemaBibliotecaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaBibliotecaApplication.class, args);
	}

	// 🔹 Este Bean é executado após a inicialização do Spring Boot
	@Bean
	CommandLineRunner init(PessoaRepository repo, PasswordEncoder encoder) {
		return args -> {
			repo.findAll().forEach(pessoa -> {
				// só criptografa se ainda não estiver em formato BCrypt
				if (!pessoa.getSenha().startsWith("$2a$")) {
					pessoa.setSenha(encoder.encode(pessoa.getSenha()));
					repo.save(pessoa);
					System.out.println("Senha criptografada para usuário: " + pessoa.getUsername());
				}
			});
		};
	}
}

