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

    // Atributos Usuário

    @Column(nullable = false)
    private Integer limiteSlots = 3;

    @ManyToMany
    private List<Emprestimo> livrosAtivos = new ArrayList<>();

    @ManyToMany
    private List<Reserva> reservasAtivas = new ArrayList<>();

    @ManyToMany(mappedBy = "usuario")
    private List<Emprestimo> emprestimos;

    public Usuario() {
        this.setFlagAtivo(true);
        this.limiteSlots = 3;
    }

    public Usuario(String nome, String email) {
        this.setNome(nome);
        this.setEmail(email);
        this.setFlagAtivo(true);
        this.limiteSlots = 3; // ✅ valor padrão
    }

    public Integer getLimiteSlots() {
        return limiteSlots != null ? limiteSlots : 3;
    }

    // Validar se o usuário foi aprovado
    private void validarAtivo() {
        if (!isFlagAtivo()) {
            throw new IllegalArgumentException("Usuário não aprovado. Aguarde a aprovação para acessar o site.");
        }

    }

    // Quantos slots já foram usados (Livros + Reservas)
    public int slotsUsados() {
        validarAtivo();
        return (livrosAtivos == null ? 0 : livrosAtivos.size()) + (reservasAtivas == null ? 0 : reservasAtivas.size());
    }

    // Disponibilidade de Slots
    public int slotsDisponiveis() {
        validarAtivo();
        return getLimiteSlots() - slotsUsados();
    }

    // Verificação de Emprestar e Reservar

    public boolean podeReservar() {
        validarAtivo();
        return slotsDisponiveis() > 0;
    }

    public boolean podeEmprestar() {
        validarAtivo();
        return slotsDisponiveis() > 0;
    }

    // Métodos Usuário

    public Emprestimo emprestarLivro(Livro livro) {
        validarAtivo();
        if (!livro.isDisponivel()) {
            throw new IllegalStateException("Livro não disponível para empréstimo");
        }
        if (!podeEmprestar()) {
            throw new IllegalStateException("Limite de empréstimos e reservas atingido");
        }
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
        if (!podeReservar()) {
            throw new IllegalStateException("Limite de empréstimos e reservas atingido");
        }
        livro.alterarStatus(Livro.Status.RESERVADO);
        Reserva r = new Reserva(this, livro);
        reservasAtivas.add(r);
        return r;

    }

    public void cancelarReserva(Livro livro) {
        validarAtivo();
        if (livro == null)
            return;

        Reserva alvo = null;
        for (Reserva r : new ArrayList<>(reservasAtivas)) {
            if (r.getLivro().equals(livro) && r.getStatus() == Reserva.ReservaStatus.ATIVA) {
                alvo = r;
                break;
            }
        }

        if (alvo == null) {
            throw new IllegalStateException("Nenhuma reserva ativa encontrada para este livro com este usuário.");
        }

        alvo.cancelar();
        // removerReserva(alvo) -> Helper -> IMPLEMENTAR
        boolean aindaTemReservaLocal = reservasAtivas.stream()
                .anyMatch(r -> r.getLivro().equals(livro) && r.getStatus() == Reserva.ReservaStatus.ATIVA);
        if (!aindaTemReservaLocal) {
            livro.alterarStatus(Livro.Status.DISPONIVEL);
        }
    }

    public List<Emprestimo> consultaHistorico() {
        validarAtivo();
        return new ArrayList<>(livrosAtivos);
    }

    public void setLimiteSlots(int i) {
    }

    public List<Emprestimo> getEmprestimos() {
        return emprestimos;
    }

    public void setEmprestimos(List<Emprestimo> emprestimos) {
        this.emprestimos = emprestimos;
    }
}
