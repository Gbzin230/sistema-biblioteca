package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_usuario")
public class Usuario extends Pessoa {

    // ============================================================
    // CAMPOS ESPECÍFICOS DO USUÁRIO
    // ============================================================

    @Column(name = "cod_status")
    private Integer codStatus;

    @Column(name = "url_documento")
    private String urlDocumento;

    @Column(name = "num_cep")
    private String cep;

    @Column(name = "dt_cadastro")
    private LocalDateTime dtCadastro;

    @Column(name = "dt_desativacao")
    private LocalDateTime dtDesativacao;

    @Column(name = "dt_banimento")
    private LocalDateTime dtBanimento;

    @Column(name = "limite_slots")
    private Integer limiteSlots;

    @Column(name = "url_capa")
    private String urlCapa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cod_role")
    private Role role;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Emprestimo> livrosAtivos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservasAtivas = new ArrayList<>();


    // ============================================================
    // CONSTRUTORES
    // ============================================================

    public Usuario() {
        this.flagAtivo = true;
        this.codStatus = 1; // 1 = PENDENTE
        this.limiteSlots = 3;
        this.dtCadastro = LocalDateTime.now();
    }

    public Usuario(Role rolePadrao) {
        this();
        this.role = rolePadrao;
    }


    // ============================================================
    // REGRAS DE NEGÓCIO
    // ============================================================

    // 🔥 ATIVA O USUÁRIO
    public void ativar() {
        this.flagAtivo = true;
        this.codStatus = 2; // 2 = ATIVO
        this.dtDesativacao = null;
    }

    // 🔥 BLOQUEIA O USUÁRIO
    public void bloquear() {
        this.flagAtivo = false;
        this.codStatus = 3; // 1 = INATIVO
        this.dtDesativacao = LocalDateTime.now();
    }

    // 🔥 DESATIVA O USUÁRIO
    public void desativar() {
        this.flagAtivo = false;
        this.codStatus = 4; // 1 = INATIVO
        this.dtDesativacao = LocalDateTime.now();
    }

    // Verifica se pode realizar operações
    private void validarAtivo() {
        if (Boolean.FALSE.equals(flagAtivo) || codStatus == null || !codStatus.equals(2)) {
            throw new IllegalStateException("Usuário não está ativo no sistema.");
        }
    }

    public boolean podeEmprestar() {
        validarAtivo();
        return (livrosAtivos.size() + reservasAtivas.size()) < limiteSlots;
    }

    public boolean podeReservar() {
        validarAtivo();
        return (livrosAtivos.size() + reservasAtivas.size()) < limiteSlots;
    }

    public Emprestimo emprestarLivro(Livro livro) {
        validarAtivo();

        if (!podeEmprestar()) {
            throw new IllegalStateException("Limite de empréstimos atingido.");
        }

        Emprestimo emp = new Emprestimo(this, livro);
        livrosAtivos.add(emp);

        return emp;
    }

    public void devolverLivro(Emprestimo emprestimo) {
        validarAtivo();
        livrosAtivos.remove(emprestimo);
    }

    public Reserva reservarLivro(Livro livro, StatusReserva statusInicial) {
        validarAtivo();

        if (!podeReservar()) {
            throw new IllegalStateException("Limite de reservas atingido.");
        }

        Reserva r = new Reserva(this, livro, statusInicial);
        reservasAtivas.add(r);

        return r;
    }

    public void cancelarReserva(Livro livro) {
        validarAtivo();

        Reserva alvo = reservasAtivas.stream()
                .filter(r -> r.getLivro().equals(livro) &&
                        r.getStatus().getNome().equalsIgnoreCase("ATIVA"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma reserva ativa desse livro."));

        alvo.getStatus().setNome("CANCELADA");
    }

    public List<Emprestimo> consultaHistorico() {
        validarAtivo();
        return new ArrayList<>(livrosAtivos);
    }


    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public Integer getCodStatus() { return codStatus; }
    public void setCodStatus(Integer codStatus) { this.codStatus = codStatus; }

    public String getUrlDocumento() { return urlDocumento; }
    public void setUrlDocumento(String urlDocumento) { this.urlDocumento = urlDocumento; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public LocalDateTime getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDateTime dtCadastro) { this.dtCadastro = dtCadastro; }

    public LocalDateTime getDtDesativacao() { return dtDesativacao; }
    public void setDtDesativacao(LocalDateTime dtDesativacao) { this.dtDesativacao = dtDesativacao; }

    public LocalDateTime getDtBanimento() { return dtBanimento; }
    public void setDtBanimento(LocalDateTime dtBanimento) { this.dtBanimento = dtBanimento; }

    public Integer getLimiteSlots() { return limiteSlots; }
    public void setLimiteSlots(Integer limiteSlots) { this.limiteSlots = limiteSlots; }

    public String getUrlCapa() { return urlCapa; }
    public void setUrlCapa(String urlCapa) { this.urlCapa = urlCapa; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<Emprestimo> getLivrosAtivos() { return livrosAtivos; }
    public List<Reserva> getReservasAtivas() { return reservasAtivas; }
}
