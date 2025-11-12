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
        this.setFlagAtivo(true);
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

    // validações e métodos de negócio (mantidos)
    private void validarAtivo() {
        if (!isFlagAtivo()) {
            throw new IllegalStateException("Usuário não aprovado. Aguarde a aprovação para acessar o site.");
        }
    }

    public int slotsUsados() {
        validarAtivo();
        return (livrosAtivos == null ? 0 : livrosAtivos.size()) + (reservasAtivas == null ? 0 : reservasAtivas.size());
    }

    public int slotsDisponiveis() {
        validarAtivo();
        return getLimiteSlots() - slotsUsados();
    }

    public boolean podeReservar() { validarAtivo(); return slotsDisponiveis() > 0; }
    public boolean podeEmprestar() { validarAtivo(); return slotsDisponiveis() > 0; }

    public Emprestimo emprestarLivro(Livro livro) {
        validarAtivo();
        if (!livro.isDisponivel()) throw new IllegalStateException("Livro não disponível para empréstimo");
        if (!podeEmprestar()) throw new IllegalStateException("Limite de empréstimos e reservas atingido");
        livro.alterarStatus(Livro.Status.EMPRESTADO);
        Emprestimo e = new Emprestimo(this, livro);
        livrosAtivos.add(e);
        return e;
    }

    public void devolverLivro(Emprestimo emprestimo) {
        validarAtivo();
        livrosAtivos.remove(emprestimo);
        emprestimo.getLivro().alterarStatus(Livro.Status.DISPONIVEL);
    }

    public Reserva reservarLivro(Livro livro) {
        validarAtivo();
        if (!podeReservar()) throw new IllegalStateException("Limite de empréstimos e reservas atingido");
        livro.alterarStatus(Livro.Status.RESERVADO);
        Reserva r = new Reserva(this, livro);
        reservasAtivas.add(r);
        return r;
    }

    public void cancelarReserva(Livro livro) {
        validarAtivo();
        if (livro == null) return;
        Reserva alvo = null;
        for (Reserva r : new ArrayList<>(reservasAtivas)) {
            if (r.getLivro().equals(livro) && r.getStatus() == Reserva.ReservaStatus.ATIVA) {
                alvo = r;
                break;
            }
        }
        if (alvo == null) throw new IllegalStateException("Nenhuma reserva ativa encontrada para este livro com este usuário.");
        alvo.cancelar();
        boolean aindaTemReservaLocal = reservasAtivas.stream()
                .anyMatch(r -> r.getLivro().equals(livro) && r.getStatus() == Reserva.ReservaStatus.ATIVA);
        if (!aindaTemReservaLocal) livro.alterarStatus(Livro.Status.DISPONIVEL);
    }

    public List<Emprestimo> consultaHistorico() {
        validarAtivo();
        return new ArrayList<>(livrosAtivos);
    }
}
