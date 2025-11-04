package com.biblioteca.sistema_biblioteca.dto;

public record ApiResponse<T>(T data, String message) {}