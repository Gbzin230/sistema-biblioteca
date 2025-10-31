package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public class Pessoa {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String nome;
    private String dtNascimento;
    private String email;
    private String senha;
    private String telefone;
    private String endereco;
    private String cpf;
    private char sexo;

    private boolean flagAtivo = true;

    // Métodos - EXCLUIR E USAR AUTH SERVICE
    public boolean login(String senha) {
        return this.senha != null && this.senha.equals(senha);
    }

    public void logout() {

    }
}
