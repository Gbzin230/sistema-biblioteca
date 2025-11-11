package com.biblioteca.sistema_biblioteca.dto;

public class PessoaResponseDTO {

    private Long id;
    private String username;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private String endereco;
    private String sexo;
    private Integer limiteSlots = 3;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Integer getLimiteSlots() {
        return limiteSlots;
    }

    public void setLimiteSlots(Integer limiteSlots) {
        this.limiteSlots = limiteSlots;
    }
}

