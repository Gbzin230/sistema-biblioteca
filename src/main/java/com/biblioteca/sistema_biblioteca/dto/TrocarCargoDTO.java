package com.biblioteca.sistema_biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record TrocarCargoDTO(

    @NotBlank(message = "O username é obrigatório.")
    String username,

    @NotBlank(message = "O CPF é obrigatório.")
    String cpf,

    @NotBlank(message = "O novo cargo é obrigatório. Ex: ADMIN, FUNCIONARIO, USUARIO")
    String novoRole

) {}
