package com.biblioteca.sistema_biblioteca.model.converter;

import com.biblioteca.sistema_biblioteca.model.Reserva;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ReservaStatusConverter implements AttributeConverter<Reserva.ReservaStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Reserva.ReservaStatus status) {
        if (status == null) return null;
        return switch (status) {
            case ATIVA -> 1;
            case CONFIRMADA -> 2;
            case CANCELADA -> 3;
        };
    }

    @Override
    public Reserva.ReservaStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return Reserva.ReservaStatus.ATIVA;
        return switch (dbData) {
            case 1 -> Reserva.ReservaStatus.ATIVA;
            case 2 -> Reserva.ReservaStatus.CONFIRMADA;
            case 3 -> Reserva.ReservaStatus.CANCELADA;
            default -> Reserva.ReservaStatus.ATIVA;
        };
    }
}