package com.biblioteca.sistema_biblioteca.dto;

import java.time.LocalDate;

public class EmprestimoResponseDTO {

    private Long id;
    private String usuarioId;
    private Long livroId;
    private LocalDate dtInicio;
    private LocalDate dtPrevistaDevolucao;
    private Integer numRenovacoes;
    private String status;
    private String tituloLivro;
    private String uriImgLivro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }

    public String getTituloLivro() { return tituloLivro; }
    public void setTituloLivro(String tituloLivro) { this.tituloLivro = tituloLivro; }

    public String getUriImgLivro() { return uriImgLivro; }
    public void setUriImgLivro(String uriImgLivro) { this.uriImgLivro = uriImgLivro; }

    public LocalDate getDtInicio() { return dtInicio; }
    public void setDtInicio(LocalDate dtInicio) { this.dtInicio = dtInicio; }

    public LocalDate getDtPrevistaDevolucao() { return dtPrevistaDevolucao; }
    public void setDtPrevistaDevolucao(LocalDate dtPrevistaDevolucao) { this.dtPrevistaDevolucao = dtPrevistaDevolucao; }

    public Integer getNumRenovacoes() { return numRenovacoes; }
    public void setNumRenovacoes(Integer numRenovacoes) { this.numRenovacoes = numRenovacoes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
