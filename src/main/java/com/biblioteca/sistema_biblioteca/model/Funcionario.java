package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("FUNCIONARIO")
public class Funcionario extends Pessoa {

    // Atributos

    // Métodos Funcionario

    // public Livro cadastrarLivro(Livro livro) {
    // // Lógica
    // return null;
    // }

    // public void inativarLivro(Livro livro) {
    // // Lógica
    // }

    // public void ativarLivro(Livro livro) {
    // // Lógica
    // }

    // public List<Usuario> consultarUsuarios() {
    // // Lógica
    // return null;
    // }

    // public List<Emprestimo> consultarHistoricoUsuario(Usuario usuario) {
    // // Lógica
    // return null;
    // }

    // public List<Livro> consultarLivros() {
    // // Lógica
    // return null;
    // }

    // public Boolean aprovarUsuario() {
    // // Lógica
    // return false;
    // }
}
