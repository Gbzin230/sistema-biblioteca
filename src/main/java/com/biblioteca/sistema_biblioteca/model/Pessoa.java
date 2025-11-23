package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Pessoa {

    // corresponde ao cod_username VARCHAR(255) PK em tb_usuario
    @Id
    @Column(name = "cod_username", nullable = false, updatable = false)
    protected String username;

    @Column(name = "txt_nome")
    protected String nome;

    @Column(name = "dt_nascimento")
    protected String dtNascimento;

    @Column(name = "senha_hash")
    protected String senha;

    @Column(name = "txt_email")
    protected String email;

    @Column(name = "num_telefone")
    protected String telefone;

    @Column(name = "num_endereco")
    protected String endereco;

    @Column(name = "cod_cpf", unique = true)
    protected String cpf;

    @Column(name = "char_sexo")
    protected Character sexo;

    @Column(name = "flag_ativo")
    protected Boolean flagAtivo;

    // =======================
    // GETTERS / SETTERS
    // =======================

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDtNascimento() { return dtNascimento; }
    public void setDtNascimento(String dtNascimento) { this.dtNascimento = dtNascimento; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Character getSexo() { return sexo; }
    public void setSexo(Character sexo) { this.sexo = sexo; }

    public Boolean getFlagAtivo() { return flagAtivo; }
    public void setFlagAtivo(Boolean flagAtivo) { this.flagAtivo = flagAtivo; }
}
