package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.model.Reserva;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;
import com.biblioteca.sistema_biblioteca.repository.AutorRepository;
import com.biblioteca.sistema_biblioteca.repository.TemaRepository;
import com.biblioteca.sistema_biblioteca.repository.TagRepository;
import com.biblioteca.sistema_biblioteca.repository.EditoraRepository;
import com.biblioteca.sistema_biblioteca.repository.EmprestimoRepository;
import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;
    private final AutorRepository autorRepository;
    private final TemaRepository temaRepository;
    private final TagRepository tagRepository;
    private final EditoraRepository editoraRepository;

    public LivroService(LivroRepository livroRepository,
                        EmprestimoRepository emprestimoRepository,
                        ReservaRepository reservaRepository,
                        AutorRepository autorRepository,
                        TemaRepository temaRepository,
                        TagRepository tagRepository,
                        EditoraRepository editoraRepository) {
        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.autorRepository = autorRepository;
        this.temaRepository = temaRepository;
        this.tagRepository = tagRepository;
        this.editoraRepository = editoraRepository;
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
        // Resolver Autor/Tema/Tags/Editora a partir dos campos transitórios
        if (livro.getAutor() != null && !livro.getAutor().isBlank()) {
            Autor autor = autorRepository.findByNomeIgnoreCase(livro.getAutor())
                    .orElseGet(() -> { var a = new Autor(); a.setNome(livro.getAutor().trim()); return autorRepository.save(a); });
            livro.setAutores(Set.of(autor));
        }
        if (livro.getTema() != null && !livro.getTema().isBlank()) {
            Tema tema = temaRepository.findByNomeIgnoreCase(livro.getTema())
                    .orElseGet(() -> { var t = new Tema(); t.setNome(livro.getTema().trim()); return temaRepository.save(t); });
            livro.setTemas(Set.of(tema));
        }
        if (livro.getTags() != null && !livro.getTags().isEmpty()) {
            var tags = livro.getTags().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .map(nome -> tagRepository.findByNomeIgnoreCase(nome)
                            .orElseGet(() -> { var tg = new Tag(); tg.setNome(nome); return tagRepository.save(tg); }))
                    .collect(java.util.stream.Collectors.toSet());
            livro.setTagsEntidades(tags);
        }
        if (livro.getEditora() != null && !livro.getEditora().isBlank()) {
            Editora editora = editoraRepository.findByNomeIgnoreCase(livro.getEditora())
                    .orElseGet(() -> { var e = new Editora(); e.setNome(livro.getEditora().trim()); return editoraRepository.save(e); });
            livro.setEditoraEntidade(editora);
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

                    // Atualizar relacionamentos
                    if (livroAtualizado.getAutor() != null && !livroAtualizado.getAutor().isBlank()) {
                        Autor autor = autorRepository.findByNomeIgnoreCase(livroAtualizado.getAutor())
                                .orElseGet(() -> { var a = new Autor(); a.setNome(livroAtualizado.getAutor().trim()); return autorRepository.save(a); });
                        livro.setAutores(java.util.Set.of(autor));
                    } else {
                        livro.setAutores(null);
                    }
                    if (livroAtualizado.getTema() != null && !livroAtualizado.getTema().isBlank()) {
                        Tema tema = temaRepository.findByNomeIgnoreCase(livroAtualizado.getTema())
                                .orElseGet(() -> { var t = new Tema(); t.setNome(livroAtualizado.getTema().trim()); return temaRepository.save(t); });
                        livro.setTemas(java.util.Set.of(tema));
                    } else {
                        livro.setTemas(null);
                    }
                    if (livroAtualizado.getTags() != null && !livroAtualizado.getTags().isEmpty()) {
                        var tags = livroAtualizado.getTags().stream()
                                .filter(s -> s != null && !s.isBlank())
                                .map(String::trim)
                                .map(nome -> tagRepository.findByNomeIgnoreCase(nome)
                                        .orElseGet(() -> { var tg = new Tag(); tg.setNome(nome); return tagRepository.save(tg); }))
                                .collect(java.util.stream.Collectors.toSet());
                        livro.setTagsEntidades(tags);
                    } else {
                        livro.setTagsEntidades(null);
                    }
                    if (livroAtualizado.getEditora() != null && !livroAtualizado.getEditora().isBlank()) {
                        Editora editora = editoraRepository.findByNomeIgnoreCase(livroAtualizado.getEditora())
                                .orElseGet(() -> { var e = new Editora(); e.setNome(livroAtualizado.getEditora().trim()); return editoraRepository.save(e); });
                        livro.setEditoraEntidade(editora);
                    } else {
                        livro.setEditoraEntidade(null);
                    }
                    return livroRepository.save(livro);
                })
                .orElseThrow(() -> new RuntimeException("Livro não encontrado para atualização"));
    }

    // ✅ DELETAR
    @Transactional
    public void deletar(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));

        // Verifica se há empréstimo ativo com esse livro
        boolean emprestado = emprestimoRepository.findAll().stream()
                .anyMatch(e -> e.getLivro().equals(livro) && e.getStatus() == Emprestimo.Status.ATIVO);

        // Verifica se há reserva ativa com esse livro
        boolean reservado = reservaRepository.findAll().stream()
                .anyMatch(r -> r.getLivro().equals(livro) && r.getStatus() == Reserva.ReservaStatus.ATIVA);

        if (emprestado || reservado) {
            throw new RegraNegocioException(
                    "Não é possível deletar o livro. Ele está associado a um empréstimo ou reserva ativa."
            );
        }

        livroRepository.delete(livro);
    }


    // ✅ CONSULTAR FILA DE RESERVAS
    public int consultarListaReserva(Livro livro) {
        List<Reserva> fila = reservaRepository.findByLivroAndStatusOrderByDtSolicitacaoAsc(
                livro, Reserva.ReservaStatus.ATIVA);
        return fila.size();
    }

    // ✅ LISTAR COM BUSCA PAGINADA
    public Page<Livro> listar(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return livroRepository.findAll(pageable);
        }
        // Busca combinada por título, autor, tema e tags via JOINs
        return livroRepository.searchByTituloAutorTemaTag(q, pageable);
    }
}

