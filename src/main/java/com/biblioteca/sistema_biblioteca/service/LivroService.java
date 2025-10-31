package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final ReservaRepository reservaRepository;

    public LivroService(LivroRepository livroRepository, ReservaRepository reservaRepository) {
        this.livroRepository = livroRepository;
        this.reservaRepository = reservaRepository;
    }

    // CRUD
    @Transactional
    public Livro salvarLivro(Livro livro) {
        if (livro.getFlagAtivo() == null) {
            livro.setFlagAtivo(Boolean.TRUE);
        }
        if (livro.getStatus() == null) {
            livro.setStatus(Livro.Status.DISPONIVEL);
        }
        return livroRepository.save(livro);
    }

    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }

    public Optional<Livro> buscaPorId(Long id) {
        return livroRepository.findById(id);
    }

    public Optional<Livro> atualizarLivro(Long id, Livro livroAtualizado) {
        return livroRepository.findById(id).map(livro -> {
            livro.setTitulo(livroAtualizado.getTitulo());
            livro.setAutor(livroAtualizado.getAutor());
            livro.setEditora(livroAtualizado.getEditora());
            livro.setTema(livroAtualizado.getTema());
            livro.setTags(livroAtualizado.getTags());
            livro.setAnoLancamento(livroAtualizado.getAnoLancamento());
            livro.setFlagAtivo(livroAtualizado.getFlagAtivo());
            livro.setStatus(livroAtualizado.getStatus());
            livro.setSinopse(livroAtualizado.getSinopse());
            return livroRepository.save(livro);
        });
    }

    public void deletarLivro(Long id) {
        if (livroRepository.existsById(id)) {
            livroRepository.deleteById(id);
        }
    }

    // Métodos

    public int consultarListaReserva(Livro livro) {
        return reservaRepository.findByLivroAndStatusOrderByPosicaoFila(livro, Reserva.ReservaStatus.ATIVA).size();
    }

}
