package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Editora;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EditoraRepository extends JpaRepository<Editora, Long> {
    Optional<Editora> findByNomeIgnoreCase(String nome);
}