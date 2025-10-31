package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.model.Reserva.ReservaStatus;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.service.EmprestimoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoService emprestimoService;

    public ReservaService(ReservaRepository reservaRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoService emprestimoService) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoService = emprestimoService;
    }

    @Transactional
    public Reserva criarReserva(Long usuarioId, Long livroId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        List<Reserva> fila = reservaRepository.findByLivroAndStatusOrderByPosicaoFila(livro, ReservaStatus.ATIVA);
        int posicao = fila.size() + 1;

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLivro(livro);
        reserva.setPosicaoFila(posicao);
        reserva.setStatus(Reserva.ReservaStatus.ATIVA);

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Emprestimo confirmarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        Emprestimo emprestimo = reserva.confirmar();
        if (emprestimo == null) {
            throw new RuntimeException("Reserva não pode ser confirmada");
        }

        reservaRepository.save(reserva);
        return emprestimoService.criarEmprestimo(
                reserva.getUsuario().getId(),
                reserva.getLivro().getId());
    }

    @Transactional
    public void cancelarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        reserva.cancelar();
        reservaRepository.save(reserva);
    }
}
