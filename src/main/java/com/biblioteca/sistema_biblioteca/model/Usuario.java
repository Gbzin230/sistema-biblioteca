package com.biblioteca.sistema_biblioteca.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("USUARIO")
public class Usuario extends Pessoa {

    @Column(nullable = true)
    private Integer limiteSlots = 3;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Emprestimo> livrosAtivos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservasAtivas = new ArrayList<>();

    public Usuario() {
        this.setFlagAtivo(false);
        this.limiteSlots = 3;
    }

    public Usuario(String nome, String email) {
        this();
        this.setNome(nome);
        this.setEmail(email);
    }

    public Integer getLimiteSlots() {
        return limiteSlots != null ? limiteSlots : 3;
    }

    public void setLimiteSlots(Integer limiteSlots) {
        this.limiteSlots = limiteSlots;
    }

    // ======================
    // Validações
    // ======================

    private void validarAtivo() {
        if (!isFlagAtivo()) {
            throw new IllegalStateException("Usuário não aprovado. Aguarde a aprovação para acessar o sistema.");
        }
    }

    public int slotsUsados() {
        validarAtivo();
        return (livrosAtivos == null ? 0 : livrosAtivos.size())
             + (reservasAtivas == null ? 0 : reservasAtivas.size());
    }

    public int slotsDisponiveis() {
        validarAtivo();
        return getLimiteSlots() - slotsUsados();
    }

    public boolean podeReservar() { validarAtivo(); return slotsDisponiveis() > 0; }
    public boolean podeEmprestar() { validarAtivo(); return slotsDisponiveis() > 0; }

    // ======================
    // Regras de Empréstimo
    // ======================

    public Emprestimo emprestarLivro(Livro livro) {
        validarAtivo();

        if (!podeEmprestar())
            throw new IllegalStateException("Limite de empréstimos e reservas atingido");

        Emprestimo emprestimo = new Emprestimo(this, livro);

        livrosAtivos.add(emprestimo);

        return emprestimo;
    }

    public void devolverLivro(Emprestimo emprestimo) {
        validarAtivo();
        livrosAtivos.remove(emprestimo);
    }

    // ======================
    // Regras de Reserva
    // ======================

    public Reserva reservarLivro(Livro livro) {
        validarAtivo();

        if (!podeReservar())
            throw new IllegalStateException("Limite de empréstimos e reservas atingido");

        Reserva reserva = new Reserva(this, livro);
        reservasAtivas.add(reserva);

        return reserva;
    }

    public void cancelarReserva(Livro livro) {
        validarAtivo();
        if (livro == null) return;

        Reserva alvo = reservasAtivas.stream()
                .filter(r -> r.getLivro().equals(livro)
                          && r.getStatus() == Reserva.ReservaStatus.ATIVA)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma reserva ativa encontrada para este livro."));

        alvo.cancelar();
    }

    public List<Emprestimo> consultaHistorico() {
        validarAtivo();
        return new ArrayList<>(livrosAtivos);
    }
}
