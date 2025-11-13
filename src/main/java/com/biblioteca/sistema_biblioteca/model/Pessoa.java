package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_USUARIO")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_usuario")
    private Long id;

    @Column(name = "cod_username", unique = true, nullable = false)
    private String username;

    @Column(name = "txt_nome")
    private String nome;

    @Column(name = "dt_nascimento")
    private String dtNascimento;

    @Column(name = "txt_email")
    private String email;

    @Column(name = "senha_hash")
    private String senha;

    @Column(name = "num_telefone")
    private String telefone;

    @Column(name = "num_endereco")
    private String endereco;

    @Column(name = "cod_cpf")
    private String cpf;

    @Column(name = "char_sexo")
    private char sexo;

    @Column(name = "role", insertable = false, updatable = false)
    private String roleString;

    @Column(name = "flag_ativo")
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

    public String getRoleString() { return roleString != null ? roleString : this.getClass().getSimpleName().toUpperCase(); }
    public void setRoleString(String roleString) { this.roleString = roleString; }

    @PrePersist
    private void prePersist() {
        // 🔒 Por padrão, mantém o que foi definido pelo Controller
        // Se for nulo (não setado manualmente), assume false (aguardando aprovação)
        if (!this.flagAtivo) {
            this.flagAtivo = false;
        }
    }
}

