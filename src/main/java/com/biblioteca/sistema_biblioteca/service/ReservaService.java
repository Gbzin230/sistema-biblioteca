package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final EmprestimoService emprestimoService;

    public ReservaService(ReservaRepository reservaRepository,
                          LivroRepository livroRepository,
                          EmprestimoService emprestimoService) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.emprestimoService = emprestimoService;
    }

    @Transactional
    public Reserva criarReserva(Reserva reserva) {
        if (reserva.getUsuario() == null || reserva.getLivro() == null) {
            throw new RegraNegocioException("Reserva deve conter usuário e livro.");
        }

        reserva.setStatus(Reserva.ReservaStatus.ATIVA);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void confirmarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        Livro livro = reserva.getLivro();
        if (!livro.isDisponivel()) {
            throw new RegraNegocioException("Livro não disponível para empréstimo.");
        }

        emprestimoService.realizarEmprestimo(reserva.getUsuario().getId(), livro.getId());
        reserva.setStatus(Reserva.ReservaStatus.CONFIRMADA);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada"));

        reserva.cancelar();
        reservaRepository.save(reserva);

        Livro livro = reserva.getLivro();
        boolean aindaReservado = reservaRepository.existsByLivroAndStatus(livro, Reserva.ReservaStatus.ATIVA);

        if (!aindaReservado && livro.getStatus() == Livro.Status.RESERVADO) {
            livro.alterarStatus(Livro.Status.DISPONIVEL);
            livroRepository.save(livro);
        }
    }

    @Transactional(readOnly = true)
    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }
}


