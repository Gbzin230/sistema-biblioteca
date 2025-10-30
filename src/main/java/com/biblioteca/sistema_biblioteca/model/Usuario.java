package com.biblioteca.sistema_biblioteca.model;

import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
public class Usuario extends Pessoa {

    // Atributos Usuário

    private int limiteSlots;

    @OneToMany
    private List<Emprestimo> livrosAtivos;

    @OneToMany
    private List<Reserva> reservasAtivas;

    // Métodos Usuário

    public Emprestimo emprestarLivro(Livro livro) {
        // Lógica
        return null;
    }

    public void devolverLivro(Emprestimo emprestimo) {
        // Lógica
    }

    public void reservarLivro(Livro livro) {
        // Lógica

    }

    public void cancelarReserva(Livro livro) {
        // Lógica

    }

    public List<Emprestimo> consultaHistorico() {
        // Lógica
        return null;
    }

}
