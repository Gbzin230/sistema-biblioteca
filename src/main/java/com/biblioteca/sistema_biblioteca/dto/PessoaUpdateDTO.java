package com.biblioteca.sistema_biblioteca.dto;

public class PessoaUpdateDTO {

    private String nome;
    private String email;
    private String telefone;
    private String endereco;
    private Character sexo;
    private String senha;
    private String cep;
    private String cpf;
    private String dtNascimento;


    // Getters e setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public Character getSexo() { return sexo; }
    public void setSexo(Character sexo) { this.sexo = sexo; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }


    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getDtNascimento() { return dtNascimento; }
    public void setDtNascimento(String dtNascimento) { this.dtNascimento = dtNascimento;}

}
