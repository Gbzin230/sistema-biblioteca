package com.biblioteca.sistema_biblioteca.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;

        Converter<String, LocalDate> stringToLocalDate = new Converter<>() {
            @Override
            public LocalDate convert(MappingContext<String, LocalDate> context) {
                String source = context.getSource();
                return (source == null || source.isBlank()) ? null : LocalDate.parse(source, iso);
            }
        };

        Converter<LocalDate, String> localDateToString = new Converter<>() {
            @Override
            public String convert(MappingContext<LocalDate, String> context) {
                LocalDate source = context.getSource();
                return source == null ? null : source.format(iso);
            }
        };

        mapper.addConverter(stringToLocalDate);
        mapper.addConverter(localDateToString);

        return mapper;
    }
}

