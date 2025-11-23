package com.biblioteca.sistema_biblioteca.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_LIVRO")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_livro")
    private Long id;

    @Column(name = "txt_titulo")
    private String titulo;

    @Transient
    private String autor;

    @Transient
    private String editora;

    @Transient
    private String tema;

    @Transient
    private List<String> tags;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_LIVRO_AUTOR",
            joinColumns = @JoinColumn(name = "cod_livro"),
            inverseJoinColumns = @JoinColumn(name = "cod_autor"))
    private Set<Autor> autores;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_LIVRO_TEMA",
            joinColumns = @JoinColumn(name = "cod_livro"),
            inverseJoinColumns = @JoinColumn(name = "cod_tema"))
    private Set<Tema> temas;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_LIVRO_TAGS",
            joinColumns = @JoinColumn(name = "cod_livro"),
            inverseJoinColumns = @JoinColumn(name = "cod_tag"))
    private Set<Tag> tagsEntidades;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_editora")
    private Editora editoraEntidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_obra")
    private Obra obraEntidade;

    @Column(name = "ano_lancamento")
    private String anoLancamento;

    @Column(name = "flag_ativo")
    private Boolean flagAtivo;

    @Column(name = "txt_sinopse", columnDefinition = "TEXT")
    private String sinopse;

    @Column(name = "num_total_licencas")
    private Integer quantidadeDisponivel;

    @Column(name = "uri_img_livro")
    private String uriImgLivro;

    @Column(name = "url_livro")
    private String uriArquivoLivro;

    @Column(name = "dt_validade")
    private String dtValidade;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "cod_status", nullable = false)
    private StatusLivro status;

    public boolean isDisponivel() {
        return flagAtivo != null
                && flagAtivo
                && status != null
                && status.getNome().equalsIgnoreCase("DISPONIVEL");
    }

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

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Set<Autor> getAutores() { return autores; }
    public void setAutores(Set<Autor> autores) { this.autores = autores; }

    public Set<Tema> getTemas() { return temas; }
    public void setTemas(Set<Tema> temas) { this.temas = temas; }

    public Set<Tag> getTagsEntidades() { return tagsEntidades; }
    public void setTagsEntidades(Set<Tag> tagsEntidades) { this.tagsEntidades = tagsEntidades; }

    public Editora getEditoraEntidade() { return editoraEntidade; }
    public void setEditoraEntidade(Editora editoraEntidade) { this.editoraEntidade = editoraEntidade; }

    public Obra getObraEntidade() { return obraEntidade; }
    public void setObraEntidade(Obra obraEntidade) { this.obraEntidade = obraEntidade; }

    @PostLoad
    private void preencherTransitórios() {

        if (autores != null && autor == null)
            autor = autores.stream().findFirst().map(Autor::getNome).orElse(null);

        if (temas != null && tema == null)
            tema = temas.stream().findFirst().map(Tema::getNome).orElse(null);

        if (tagsEntidades != null && (tags == null || tags.isEmpty()))
            tags = tagsEntidades.stream().map(Tag::getNome).collect(Collectors.toList());

        if (editoraEntidade != null && editora == null)
            editora = editoraEntidade.getNome();
    }

    public String getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(String anoLancamento) { this.anoLancamento = anoLancamento; }

    public Boolean getFlagAtivo() { return flagAtivo; }
    public void setFlagAtivo(Boolean flagAtivo) { this.flagAtivo = flagAtivo; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public StatusLivro getStatus() { return status; }
    public void setStatus(StatusLivro status) { this.status = status; }

    public Integer getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) { this.quantidadeDisponivel = quantidadeDisponivel; }

    public String getUriImgLivro() { return uriImgLivro; }
    public void setUriImgLivro(String uriImgLivro) { this.uriImgLivro = uriImgLivro; }

    public String getUriArquivoLivro() { return uriArquivoLivro; }
    public void setUriArquivoLivro(String uriArquivoLivro) { this.uriArquivoLivro = uriArquivoLivro; }

    public String getDtValidade() { return dtValidade; }
    public void setDtValidade(String dtValidade) { this.dtValidade = dtValidade; }
}
