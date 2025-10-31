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

    private String titulo;
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
        RESERVADO,
        INATIVO

    }

    // comportamentos do domínio (regras de negócio)

    public Status consultarStatus() {
        return this.status;
    }

    public void alterarStatus(Status novoStatus) {
        if (Boolean.TRUE.equals(this.flagAtivo)) {
            this.status = novoStatus;
        } else {
            throw new IllegalStateException("Não é possível alterar o status, pois o livro está inativo");
        }
    }

    public boolean isDisponivel() {
        return this.status == Status.DISPONIVEL && Boolean.TRUE.equals(this.flagAtivo);
    }

}