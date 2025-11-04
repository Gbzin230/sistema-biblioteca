package com.biblioteca.sistema_biblioteca.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        String erro,
        String mensagem,
        int status,
        LocalDateTime timestamp,
        Map<String, String> detalhes
) {}