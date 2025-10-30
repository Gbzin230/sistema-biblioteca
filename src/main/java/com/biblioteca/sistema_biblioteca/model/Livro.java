package com.biblioteca.sistema_biblioteca.model;

import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String autor;
    private String editora;
    private String tema;

    @ElementCollection
    private List<String> tags;

    private Integer anoLancamento;
    private Boolean flagAtivo;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 2000)
    private String sinopse;

    public enum Status {
        DISPONIVEL,
        EMPRESTADO,
        RESERVADO

    }

}