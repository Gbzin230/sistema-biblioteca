package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    Page<Livro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
    Page<Livro> findByAutorContainingIgnoreCase(String autor, Pageable pageable);
    Page<Livro> findByTituloContainingIgnoreCaseAndAutorContainingIgnoreCase(String titulo, String autor, Pageable pageable);
    Page<Livro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(String titulo, String autor, Pageable pageable);
}
