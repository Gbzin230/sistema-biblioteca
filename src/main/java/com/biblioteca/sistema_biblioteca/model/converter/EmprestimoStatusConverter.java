package com.biblioteca.sistema_biblioteca.model.converter;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EmprestimoStatusConverter implements AttributeConverter<Emprestimo.Status, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Emprestimo.Status status) {
        if (status == null) return null;
        return switch (status) {
            case ATIVO -> 1;
            case FINALIZADO -> 2;
            case ATRASADO -> 3;
        };
    }

    @Override
    public Emprestimo.Status convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return Emprestimo.Status.ATIVO;
        return switch (dbData) {
            case 1 -> Emprestimo.Status.ATIVO;
            case 2 -> Emprestimo.Status.FINALIZADO;
            case 3 -> Emprestimo.Status.ATRASADO;
            default -> Emprestimo.Status.ATIVO;
        };
    }
}