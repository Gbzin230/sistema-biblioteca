package com.biblioteca.sistema_biblioteca.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.biblioteca.sistema_biblioteca.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsernameOrEmailOrCpf(String username, String email, String cpf);
    List<Usuario> findByRoleIdIn(List<Integer> roleIds);
}