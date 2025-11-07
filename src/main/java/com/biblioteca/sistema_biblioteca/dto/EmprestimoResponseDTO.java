package com.biblioteca.sistema_biblioteca.dto;

import java.time.LocalDate;

public class EmprestimoResponseDTO {

    private Long id;
    private Long usuarioId;
    private Long livroId;
    private LocalDate dtInicio;
    private LocalDate dtPrevistaDevolucao;
    private Integer numRenovacoes;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }

    public LocalDate getDtInicio() { return dtInicio; }
    public void setDtInicio(LocalDate dtInicio) { this.dtInicio = dtInicio; }

    public LocalDate getDtPrevistaDevolucao() { return dtPrevistaDevolucao; }
    public void setDtPrevistaDevolucao(LocalDate dtPrevistaDevolucao) { this.dtPrevistaDevolucao = dtPrevistaDevolucao; }

    public Integer getNumRenovacoes() { return numRenovacoes; }
    public void setNumRenovacoes(Integer numRenovacoes) { this.numRenovacoes = numRenovacoes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
