package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByNomeIgnoreCase(String nome);

}
