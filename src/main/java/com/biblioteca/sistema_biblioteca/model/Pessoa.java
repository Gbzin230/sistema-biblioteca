package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String nome;
    private String dtNascimento;
    private String email;
    private String senha;     // 🔐 importante
    private String telefone;
    private String endereco;
    private String cpf;
    private char sexo;
    private boolean flagAtivo;

    // ===== Getters e Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDtNascimento() { return dtNascimento; }
    public void setDtNascimento(String dtNascimento) { this.dtNascimento = dtNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public char getSexo() { return sexo; }
    public void setSexo(char sexo) { this.sexo = sexo; }

    public boolean isFlagAtivo() { return flagAtivo; }
    public void setFlagAtivo(boolean flagAtivo) { this.flagAtivo = flagAtivo; }

    public String getRoleString() {
        // se você quiser mapear valores diferentes:
        String simple = this.getClass().getSimpleName().toUpperCase();
        // retorna USUARIO, FUNCIONARIO, ADMIN
        return simple;
    }

    @PrePersist
    private void prePersist() {
        // 🔒 Por padrão, mantém o que foi definido pelo Controller
        // Se for nulo (não setado manualmente), assume false (aguardando aprovação)
        if (!this.flagAtivo) {
            this.flagAtivo = false;
        }
    }
}

