package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.StatusLivro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusLivroRepository extends JpaRepository<StatusLivro, Long> {

    Optional<StatusLivro> findByNomeIgnoreCase(String nome);
}
