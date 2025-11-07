package com.biblioteca.sistema_biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // ✅ Habilita agendamentos automáticos no Spring
public class SistemaBibliotecaApplication {
	public static void main(String[] args) {
		SpringApplication.run(SistemaBibliotecaApplication.class, args);
	}
}
