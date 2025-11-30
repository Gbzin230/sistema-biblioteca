package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.StatusReserva;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // 🔹 Primeira reserva da fila (ordena por data)
    Optional<Reserva> findFirstByLivroAndStatusOrderByDtInicioReservaAsc(
            Livro livro,
            StatusReserva status
    );

    // 🔹 Lista de reservas por status
    List<Reserva> findByLivroAndStatusOrderByDtInicioReservaAsc(
            Livro livro,
            StatusReserva status
    );

    List<Reserva> findByUsuarioOrderByDtInicioReservaDesc(Usuario usuario);

    // 🔹 Contagem por usuário e status
    int countByUsuarioAndStatus(
            Usuario usuario,
            StatusReserva status
    );

    // 🔹 Existe reserva ativa para o livro?
    boolean existsByLivroAndStatus(
            Livro livro,
            StatusReserva status
    );

    // 🔹 Necessário para UsuarioService.deletar()
    boolean existsByUsuario(Usuario usuario);
}
