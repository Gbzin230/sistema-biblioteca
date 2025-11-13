package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TemaRepository extends JpaRepository<Tema, Long> {
    Optional<Tema> findByNomeIgnoreCase(String nome);
}