package com.biblioteca.sistema_biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.biblioteca.sistema_biblioteca.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
