package com.biblioteca.sistema_biblioteca.model;

import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
public class Funcionario extends Pessoa {

    // Métodos Funcionario

    public Livro cadastrarLivro(Livro livro) {
        // Lógica
        return null;
    }

    public void inativarLivro(Livro livro) {
        // Lógica
    }

    public void ativarLivro(Livro livro) {
        // Lógica
    }

    public List<Usuario> consultarUsuarios() {
        // Lógica
        return null;
    }

    public List<Emprestimo> consultarHistoricoUsuario(Usuario usuario) {
        // Lógica
        return null;
    }

    public List<Livro> consultarLivros() {
        // Lógica
        return null;
    }

    public Boolean aprovarUsuario() {
        // Lógica
        return false;
    }
}
