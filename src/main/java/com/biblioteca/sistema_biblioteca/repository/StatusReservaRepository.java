package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusReservaRepository extends JpaRepository<StatusReserva, Integer> {

    Optional<StatusReserva> findByNomeIgnoreCase(String nome);
}
