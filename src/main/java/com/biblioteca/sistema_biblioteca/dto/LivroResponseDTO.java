package com.biblioteca.sistema_biblioteca.dto;

import java.util.List;

public class LivroResponseDTO {

    private Long id;

    private String titulo;
    private String autor;
    private String editora;
    private String tema;
    private String obra;

    private List<String> autores;
    private List<String> temas;
    private List<String> tags;

    private String anoLancamento;
    private Integer quantidadeDisponivel;
    private Integer quantidadeDisponivelEmprestar;
    private String sinopse;
    private String status;
    private Boolean flagAtivo;

    private String dtValidade;

    private String uriImgLivro;     // capa
    private String urlLivro;        // pdf ou url pública

    // ==== getters e setters ====
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

    public String getObra() { return obra; }
    public void setObra(String obra) { this.obra = obra; }

    public List<String> getAutores() { return autores; }
    public void setAutores(List<String> autores) { this.autores = autores; }

    public List<String> getTemas() { return temas; }
    public void setTemas(List<String> temas) { this.temas = temas; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(String anoLancamento) { this.anoLancamento = anoLancamento; }

    public Integer getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) { this.quantidadeDisponivel = quantidadeDisponivel; }

    public Integer getQuantidadeDisponivelEmprestar() { return quantidadeDisponivelEmprestar; }
    public void setQuantidadeDisponivelEmprestar(Integer quantidadeDisponivelEmprestar) { this.quantidadeDisponivelEmprestar = quantidadeDisponivelEmprestar; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getFlagAtivo() { return flagAtivo; }
    public void setFlagAtivo(Boolean flagAtivo) { this.flagAtivo = flagAtivo; }

    public String getDtValidade() { return dtValidade; }
    public void setDtValidade(String dtValidade) { this.dtValidade = dtValidade; }

    public String getUriImgLivro() { return uriImgLivro; }
    public void setUriImgLivro(String uriImgLivro) { this.uriImgLivro = uriImgLivro; }

    public String getUrlLivro() { return urlLivro; }
    public void setUrlLivro(String urlLivro) { this.urlLivro = urlLivro; }

}   
