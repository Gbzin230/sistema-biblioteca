package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public class Pessoa {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String nome;
    private String dtNascimento;
    private String email;
    private String senha;
    private String telefone;
    private String endereco;
    private String cpf;
    private char sexo;

    // Métodos
    public boolean login(String senha) {
        return this.senha != null && this.senha.equals(senha);
    }

    public void logout() {

    }
}
