package com.biblioteca.sistema_biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class LivroRequestDTO {

    @NotBlank(message = "O título é obrigatório.")
    private String titulo;

    @NotBlank(message = "O autor é obrigatório.")
    private String autor;

    private String editora;

    private String tema;

    private String obra;

    private List<String> tags = new ArrayList<>();

    private String anoLancamento;

    private String sinopse;

    private Integer quantidadeDisponivel;

    private Long codObra;           

    private String dtValidade;         // VARCHAR(8)

    private String uriImgLivro;        // caminho da capa

    private String uriArquivoLivro;    // caminho do PDF

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

    public String getObra() { return obra; }
    public void setObra(String obra) { this.obra = obra; }

    public String getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(String anoLancamento) { this.anoLancamento = anoLancamento; }

    public Integer getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) { this.quantidadeDisponivel = quantidadeDisponivel; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public Long getCodObra() { return codObra; }
    public void setCodObra(Long codObra) { this.codObra = codObra; }

    public String getDtValidade() { return dtValidade; }
    public void setDtValidade(String dtValidade) { this.dtValidade = dtValidade; }

    public String getUriImgLivro() { return uriImgLivro; }
    public void setUriImgLivro(String uriImgLivro) { this.uriImgLivro = uriImgLivro; }

    public String getUriArquivoLivro() { return uriArquivoLivro; }
    public void setUriArquivoLivro(String uriArquivoLivro) { this.uriArquivoLivro = uriArquivoLivro; }

    
}
