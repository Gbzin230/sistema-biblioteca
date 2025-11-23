package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.StatusEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusEmprestimoRepository extends JpaRepository<StatusEmprestimo, Integer> {

    Optional<StatusEmprestimo> findByNomeIgnoreCase(String nome);
}
