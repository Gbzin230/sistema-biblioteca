package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // 🔹 Primeira reserva da fila (ordena por dtInicioReserva)
    Optional<Reserva> findFirstByLivroAndStatusOrderByDtInicioReservaAsc(
            Livro livro,
            Reserva.ReservaStatus status
    );

    // 🔹 Lista de reservas ordenadas
    List<Reserva> findByLivroAndStatusOrderByDtInicioReservaAsc(
            Livro livro,
            Reserva.ReservaStatus status
    );

    // 🔹 Contagem de reservas por usuário + status
    int countByUsuarioAndStatus(
            Usuario usuario,
            Reserva.ReservaStatus status
    );

    // 🔹 Verifica se existe uma reserva ativa para o livro
    boolean existsByLivroAndStatus(
            Livro livro,
            Reserva.ReservaStatus status
    );

    // 🔹 Necessário para UsuarioService.deletar()
    boolean existsByUsuario(Usuario usuario);
}
    