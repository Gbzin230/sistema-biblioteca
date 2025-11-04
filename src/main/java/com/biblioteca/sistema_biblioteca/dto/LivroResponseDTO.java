package com.biblioteca.sistema_biblioteca.dto;

import com.biblioteca.sistema_biblioteca.model.Livro;

public class LivroResponseDTO {

    private Long id;
    private String titulo;
    private String autor;
    private String editora;
    private String tema;
    private String tags;
    private Integer anoLancamento;
    private String sinopse;
    private Livro.Status status;
    private Boolean flagAtivo;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public Livro.Status getStatus() { return status; }
    public void setStatus(Livro.Status status) { this.status = status; }

    public Boolean getFlagAtivo() { return flagAtivo; }
    public void setFlagAtivo(Boolean flagAtivo) { this.flagAtivo = flagAtivo; }
}
