package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Obra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ObraRepository extends JpaRepository<Obra, Long> {

    Optional<Obra> findByNomeIgnoreCase(String nomeObra);
}
