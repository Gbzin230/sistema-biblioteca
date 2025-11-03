package com.biblioteca.sistema_biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI bibliotecaApiDoc() {
        return new OpenAPI()
                .info(new Info()
                        .title("📚 API - Sistema de Biblioteca Virtual")
                        .version("3.5.7")
                        .description("""
                                API REST para gerenciamento de uma biblioteca virtual.
                                Inclui módulos de autenticação, usuários, livros, reservas e empréstimos.
                                """)
                        .contact(new Contact()
                                .name("Equipe Biblioteca Virtual")
                                .email("contato@biblioteca.com")
                                .url("https://github.com/Guilherme-Valerio")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor Local"),
                        new Server().url("https://biblioteca-api.onrender.com").description("Servidor de Produção (Exemplo)")
                ));
    }
}
