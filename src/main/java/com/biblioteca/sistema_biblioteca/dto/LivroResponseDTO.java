package com.biblioteca.sistema_biblioteca.dto;

import com.biblioteca.sistema_biblioteca.model.Livro;
import java.util.List;

public class LivroResponseDTO {

    private String titulo;
    private String autor;
    private String editora;
    private String tema;
    private List<String> tags;
    private Integer anoLancamento;
    private String sinopse;
    private String status; // <-- AGORA É STRING
    private Boolean flagAtivo;

    // Getters e Setters
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getFlagAtivo() { return flagAtivo; }
    public void setFlagAtivo(Boolean flagAtivo) { this.flagAtivo = flagAtivo; }
}
