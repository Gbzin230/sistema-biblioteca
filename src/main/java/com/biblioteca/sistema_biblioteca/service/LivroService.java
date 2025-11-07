package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final ReservaRepository reservaRepository;

    public LivroService(LivroRepository livroRepository, ReservaRepository reservaRepository) {
        this.livroRepository = livroRepository;
        this.reservaRepository = reservaRepository;
    }

    // ✅ CRIAR LIVRO
    @Transactional
    public Livro salvar(Livro livro) {
        if (livro.getFlagAtivo() == null) {
            livro.setFlagAtivo(Boolean.TRUE);
        }
        if (livro.getStatus() == null) {
            livro.setStatus(Livro.Status.DISPONIVEL);
        }
        return livroRepository.save(livro);
    }

    // ✅ LISTAR TODOS
    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }

    // ✅ BUSCAR POR ID
    public Livro buscarPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));
    }

    // ✅ ATUALIZAR
    @Transactional
    public Livro atualizar(Long id, Livro livroAtualizado) {
        return livroRepository.findById(id)
                .map(livro -> {
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
                })
                .orElseThrow(() -> new RuntimeException("Livro não encontrado para atualização"));
    }

    // ✅ DELETAR
    @Transactional
    public void deletar(Long id) {
        if (!livroRepository.existsById(id)) {
            throw new RuntimeException("Livro não encontrado para exclusão");
        }
        livroRepository.deleteById(id);
    }

    // ✅ CONSULTAR FILA DE RESERVAS
    public int consultarListaReserva(Livro livro) {
        return reservaRepository
                .findByLivroAndStatusOrderByPosicaoFila(livro, Reserva.ReservaStatus.ATIVA)
                .size();
    }

    // ✅ LISTAR COM BUSCA PAGINADA
    public Page<Livro> listar(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return livroRepository.findAll(pageable);
        }
        return livroRepository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(q, q, pageable);
    }
}

