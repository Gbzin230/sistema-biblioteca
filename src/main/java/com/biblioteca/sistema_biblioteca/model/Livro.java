package com.biblioteca.sistema_biblioteca.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "TB_LIVRO")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_livro")
    private Long id;

    @Column(name = "txt_titulo")
    private String titulo;

    // ----------- CAMPOS TRANSIENTES (DERIVADOS) -------------
    @Transient
    private String autor;             // autor principal (um só)

    @Transient
    private List<String> autoresLista; // lista completa

    @Transient
    private String editora;

    @Transient
    private String tema;

    @Transient
    private List<String> temasLista;

    @Transient
    private List<String> tags;

    @Transient
    private String obra;

    // ----------- RELACIONAMENTOS ----------------------------
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

    // ----------- CAMPOS PRIMITIVOS --------------------------
    @Column(name = "ano_lancamento")
    private String anoLancamento;

    @Column(name = "flag_ativo")
    private Boolean flagAtivo;

    @Column(name = "txt_sinopse", columnDefinition = "TEXT")
    private String sinopse;

    @Column(name = "num_total_licencas")
    private Integer quantidadeDisponivel;

    // NOVO: quantidade derivada (não salva no banco)
    @Transient
    private Integer quantidadeDisponivelEmprestar;

    @Column(name = "uri_img_livro")
    private String uriImgLivro;

    @Column(name = "url_livro")
    private String uriArquivoLivro;

    @Column(name = "dt_validade")
    private String dtValidade;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "cod_status", nullable = false)
    private StatusLivro status;

    // ============================================================
    // POST LOAD → Preenche campos transitórios depois do SELECT
    // ============================================================
    @PostLoad
    private void preencherTransitórios() {

        // ---- AUTOR ----
        if (autores != null && !autores.isEmpty()) {
            this.autor = autores.stream()
                    .findFirst()
                    .map(Autor::getNome)
                    .orElse(null);

            this.autoresLista = autores.stream()
                    .map(Autor::getNome)
                    .collect(Collectors.toList());
        } else {
            this.autor = null;
            this.autoresLista = null;
        }

        // ---- TEMAS ----
        if (temas != null && !temas.isEmpty()) {
            this.tema = temas.stream()
                    .findFirst()
                    .map(Tema::getNome)
                    .orElse(null);

            this.temasLista = temas.stream()
                    .map(Tema::getNome)
                    .collect(Collectors.toList());
        } else {
            this.tema = null;
            this.temasLista = null;
        }

        // ---- TAGS ----
        if (tagsEntidades != null && !tagsEntidades.isEmpty()) {
            this.tags = tagsEntidades.stream()
                    .map(Tag::getNome)
                    .collect(Collectors.toList());
        } else {
            this.tags = null;
        }

        // ---- EDITORA ----
        if (editoraEntidade != null) {
            this.editora = editoraEntidade.getNome();
        } else {
            this.editora = null;
        }

        // ---- OBRA ----
        if (obraEntidade != null) {
            this.obra = obraEntidade.getNome();
        } else {
            this.obra = null;
        }
    }

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public List<String> getAutoresLista() { return autoresLista; }

    public String getEditora() { return editora; }

    public String getTema() { return tema; }
    public List<String> getTemasLista() { return temasLista; }

    public List<String> getTags() { return tags; }

    public String getObra() { return obra; }

    public String getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(String anoLancamento) { this.anoLancamento = anoLancamento; }

    public Boolean getFlagAtivo() { return flagAtivo; }
    public void setFlagAtivo(Boolean flagAtivo) { this.flagAtivo = flagAtivo; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public Integer getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) { this.quantidadeDisponivel = quantidadeDisponivel; }

    // DERIVADO — SEMPRE CALCULADO, NUNCA SALVO
    @Transient
    public Integer getQuantidadeDisponivelEmprestar() {
        return quantidadeDisponivelEmprestar;
    }

    public void setQuantidadeDisponivelEmprestar(Integer valor) {
        this.quantidadeDisponivelEmprestar = valor;
    }


    public String getUriImgLivro() { return uriImgLivro; }
    public void setUriImgLivro(String uriImgLivro) { this.uriImgLivro = uriImgLivro; }

    public String getUriArquivoLivro() { return uriArquivoLivro; }
    public void setUriArquivoLivro(String uriArquivoLivro) { this.uriArquivoLivro = uriArquivoLivro; }

    public String getDtValidade() { return dtValidade; }
    public void setDtValidade(String dtValidade) { this.dtValidade = dtValidade; }

    public StatusLivro getStatus() { return status; }
    public void setStatus(StatusLivro status) { this.status = status; }

    // Apenas para serialização limpa no JSON
    @JsonProperty("statusNome")
    public String getStatusNome() {
        return status != null ? status.getNome() : null;
    }

    // ========== SERIALIZAÇÃO LIMPA PARA AUTORES ==========
    @JsonProperty("autores")
    public List<String> getAutoresJson() {
        return autoresLista; // já preenchida no @PostLoad
    }

    // ========== SERIALIZAÇÃO LIMPA PARA TEMAS ==========
    @JsonProperty("temas")
    public List<String> getTemasJson() {
        return temasLista; // já preenchida no @PostLoad
    }


    public String getUrlLivro() {
        return uriArquivoLivro;
    }

    public void setUrlLivro(String urlLivro) {
        this.uriArquivoLivro = urlLivro;
    }

    // ===================== relacionamentos - getters/setters =====================
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

    // ===================== utilitário =====================
    /**
     * Indica se o livro está disponível para empréstimo.
     * Implementação simples: verifica se o status existe e tem nome "DISPONIVEL".
     * Mantive comportamento conservador (não altera quantidadeDisponivel).
     */
    public boolean isDisponivel() {
        if (this.status == null) return false;
        String nome = this.status.getNome();
        return nome != null && nome.equalsIgnoreCase("DISPONIVEL");
    }

    // equals / hashCode baseado em id para permitir comparações nas streams
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Livro)) return false;
        Livro livro = (Livro) o;
        return Objects.equals(id, livro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
