package com.biblioteca.sistema_biblioteca.config;

import com.biblioteca.sistema_biblioteca.dto.ApiError;
import com.biblioteca.sistema_biblioteca.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 Erros de validação (campos @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> detalhes = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> detalhes.put(err.getField(), err.getDefaultMessage()));

        ApiError apiError = new ApiError(
                "Erro de validação",
                "Um ou mais campos estão inválidos.",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                detalhes
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    // 🔹 Falha de autenticação
    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        ApiError apiError = new ApiError(
                "Credenciais inválidas",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    // 🔹 Recurso não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ApiError apiError = new ApiError(
                "Recurso não encontrado",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    // 🔹 Outros erros genéricos (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        ApiError apiError = new ApiError(
                "Erro interno no servidor",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiError> handleRegraNegocio(RegraNegocioException ex) {
        ApiError err = new ApiError("RegraNegocio", ex.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
}