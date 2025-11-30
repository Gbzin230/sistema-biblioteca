package com.biblioteca.sistema_biblioteca.dto;

public class ReservaResponseDTO {

    private Long id;
    private String usuarioId; // agora é STRING (username)
    private Long livroId;
    private String status;
    private String UriImgLivro;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getLivroId() {
        return livroId;
    }
    public void setLivroId(Long livroId) {
        this.livroId = livroId;
    }

    public String getUriImgLivro() {
        return UriImgLivro;
    }
    public void setUriImgLivro(String UriImgLivro) {
        this.UriImgLivro = UriImgLivro;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
