package com.biblioteca.sistema_biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class LivroRequestDTO {

    @NotBlank(message = "O título é obrigatório.")
    private String titulo;

    @NotBlank(message = "O autor é obrigatório.")
    private String autor;

    private String editora;

    private String tema;

    private List<String> tags = new ArrayList<>();

    private Integer anoLancamento;

    private String sinopse;

    // ============= GETTERS E SETTERS =============

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
}
