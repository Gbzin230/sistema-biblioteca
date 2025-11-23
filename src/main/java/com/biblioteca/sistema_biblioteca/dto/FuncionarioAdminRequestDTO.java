package com.biblioteca.sistema_biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FuncionarioAdminRequestDTO {

    @NotBlank(message = "O nome de usuário é obrigatório.")
    @Size(min = 3, max = 20, message = "O nome de usuário deve ter entre 3 e 20 caracteres.")
    private String username;

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotBlank(message = "O email é obrigatório.")
    @Email(message = "Formato de email inválido.")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter no mínimo 6 caracteres.")
    @Pattern(
    regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$",
    message = "A senha deve conter letra minúscula, maiúscula, número e caractere especial."
)
    private String senha;

    @NotBlank(message = "O telefone é obrigatório.")
    @Pattern(regexp = "^\\(?\\d{2}\\)? ?9?\\d{4}-?\\d{4}$", message = "Formato de telefone inválido. Exemplo: (11)91234-5678")
    private String telefone;

    @NotBlank(message = "O CPF é obrigatório.")
    @Pattern(regexp = "^\\d{11}$", message = "O CPF deve conter exatamente 11 números.")
    private String cpf;

    @NotBlank(message = "O endereço é obrigatório.")
    private String endereco;

    @NotNull(message = "O sexo é obrigatório (M ou F).")
    private Character sexo;

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public Character getSexo() { return sexo; }

    public void setSexo(Character sexo) { this.sexo = sexo; }

}

