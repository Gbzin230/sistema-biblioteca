package com.biblioteca.sistema_biblioteca.dto;

import jakarta.validation.constraints.NotNull;

public class ReservaRequestDTO {

    @NotNull(message = "username é obrigatório")
    private String username;

    @NotNull(message = "O ID de Livro é obrigatório")
    private Long livroId;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Long getLivroId() {
        return livroId;
    }
    public void setLivroId(Long livroId) {
        this.livroId = livroId;
    }
}
