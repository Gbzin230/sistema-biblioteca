package com.biblioteca.sistema_biblioteca.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByLivroAndStatusOrderByDtSolicitacaoAsc(Livro livro, Reserva.ReservaStatus status);

    Optional<Reserva> findFirstByLivroAndStatusOrderByDtSolicitacaoAsc(Livro livro, Reserva.ReservaStatus status);

    boolean existsByLivroAndStatus(Livro livro, Reserva.ReservaStatus status);

    boolean existsByUsuario(Usuario usuario);

    int countByUsuarioAndStatus(Usuario usuario, Reserva.ReservaStatus status);
}
