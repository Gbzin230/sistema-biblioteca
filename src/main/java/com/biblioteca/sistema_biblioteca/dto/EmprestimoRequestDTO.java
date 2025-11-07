package com.biblioteca.sistema_biblioteca.dto;

import jakarta.validation.constraints.NotNull;

public class EmprestimoRequestDTO {

    @NotNull(message = "usuarioId é obrigatório")
    private Long usuarioId;

    @NotNull(message = "livroId é obrigatório")
    private Long livroId;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }
}
