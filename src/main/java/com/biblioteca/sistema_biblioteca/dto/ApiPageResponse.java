package com.biblioteca.sistema_biblioteca.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record ApiPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T,R> ApiPageResponse<R> of(Page<T> page, java.util.function.Function<T,R> mapper) {
        var mapped = page.map(mapper);
        return new ApiPageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
    }
}
