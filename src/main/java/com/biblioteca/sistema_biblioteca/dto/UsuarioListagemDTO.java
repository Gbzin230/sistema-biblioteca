package com.biblioteca.sistema_biblioteca.dto;

import java.time.LocalDateTime;

public class UsuarioListagemDTO {

    private String username;
    private String status; // nome do status
    private String urlDocumento;
    private String dtNascimento;
    private String endereco;
    private String cep;
    private String cpf;
    private String telefone;
    private String email;
    private String nome;
    private LocalDateTime dtCadastro;
    private Character sexo;
    private LocalDateTime dtDesativacao;
    private LocalDateTime dtBanimento;
    private Integer limiteSlots;
    private String urlCapa;
    private Boolean flagAtivo;
    private String role;

    public UsuarioListagemDTO(
            String username,
            String status,
            String urlDocumento,
            String dtNascimento,
            String endereco,
            String cep,
            String cpf,
            String telefone,
            String email,
            String nome,
            LocalDateTime dtCadastro,
            Character sexo,
            LocalDateTime dtDesativacao,
            LocalDateTime dtBanimento,
            Integer limiteSlots,
            String urlCapa,
            Boolean flagAtivo,
            String role
    ) {
        this.username = username;
        this.status = status;
        this.urlDocumento = urlDocumento;
        this.dtNascimento = dtNascimento;
        this.endereco = endereco;
        this.cep = cep;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.nome = nome;
        this.dtCadastro = dtCadastro;
        this.sexo = sexo;
        this.dtDesativacao = dtDesativacao;
        this.dtBanimento = dtBanimento;
        this.limiteSlots = limiteSlots;
        this.urlCapa = urlCapa;
        this.flagAtivo = flagAtivo;
        this.role = role;
    }

    // GETTERS
    public String getUsername() { return username; }
    public String  getStatus() { return status; }
    public String getUrlDocumento() { return urlDocumento; }
    public String getDtNascimento() { return dtNascimento; }
    public String getEndereco() { return endereco; }
    public String getCep() { return cep; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getNome() { return nome; }
    public LocalDateTime getDtCadastro() { return dtCadastro; }
    public Character getSexo() { return sexo; }
    public LocalDateTime getDtDesativacao() { return dtDesativacao; }
    public LocalDateTime getDtBanimento() { return dtBanimento; }
    public Integer getLimiteSlots() { return limiteSlots; }
    public String getUrlCapa() { return urlCapa; }
    public Boolean getFlagAtivo() { return flagAtivo; }
    public String getRole() { return role; }
}
