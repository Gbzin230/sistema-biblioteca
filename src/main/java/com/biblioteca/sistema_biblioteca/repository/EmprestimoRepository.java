package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    List<Emprestimo> findByUsuarioAndStatus(Usuario usuario, String status);
    boolean existsByUsuario(Usuario usuario);
}
