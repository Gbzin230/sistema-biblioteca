package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;

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
    private final StatusLivroRepository statusLivroRepository;

    public LivroService(LivroRepository livroRepository,
                        EmprestimoRepository emprestimoRepository,
                        ReservaRepository reservaRepository,
                        AutorRepository autorRepository,
                        TemaRepository temaRepository,
                        TagRepository tagRepository,
                        EditoraRepository editoraRepository,
                        StatusLivroRepository statusLivroRepository) {

        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.autorRepository = autorRepository;
        this.temaRepository = temaRepository;
        this.tagRepository = tagRepository;
        this.editoraRepository = editoraRepository;
        this.statusLivroRepository = statusLivroRepository;
    }

    // ===============================================================
    // SALVAR LIVRO
    // ===============================================================
    @Transactional
    public Livro salvar(Livro livro) {

        if (livro.getFlagAtivo() == null)
            livro.setFlagAtivo(Boolean.TRUE);

        // Status default = DISPONIVEL
        if (livro.getStatus() == null) {
            StatusLivro disponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                    .orElseThrow(() -> new RegraNegocioException("Status 'DISPONIVEL' não existe."));
            livro.setStatus(disponivel);
        }

        // =========== AUTOR ===========
        if (livro.getAutor() != null && !livro.getAutor().isBlank()) {
            Autor autor = autorRepository.findByNomeIgnoreCase(livro.getAutor())
                    .orElseGet(() -> {
                        Autor novo = new Autor();
                        novo.setNome(livro.getAutor());
                        return autorRepository.save(novo);
                    });
            livro.setAutores(Set.of(autor));
        }

        // =========== TEMA ===========
        if (livro.getTema() != null && !livro.getTema().isBlank()) {
            Tema tema = temaRepository.findByNomeIgnoreCase(livro.getTema())
                    .orElseGet(() -> {
                        Tema novo = new Tema();
                        novo.setNome(livro.getTema());
                        return temaRepository.save(novo);
                    });
            livro.setTemas(Set.of(tema));
        }

        // =========== TAGS ===========
        if (livro.getTags() != null && !livro.getTags().isEmpty()) {
            var tags = livro.getTags().stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(nome -> tagRepository.findByNomeIgnoreCase(nome)
                            .orElseGet(() -> {
                                Tag t = new Tag();
                                t.setNome(nome);
                                return tagRepository.save(t);
                            })
                    )
                    .collect(java.util.stream.Collectors.toSet());
            livro.setTagsEntidades(tags);
        }

        // =========== EDITORA ===========
        if (livro.getEditora() != null && !livro.getEditora().isBlank()) {
            Editora editora = editoraRepository.findByNomeIgnoreCase(livro.getEditora())
                    .orElseGet(() -> {
                        Editora e = new Editora();
                        e.setNome(livro.getEditora());
                        return editoraRepository.save(e);
                    });
            livro.setEditoraEntidade(editora);
        }

        return livroRepository.save(livro);
    }

    // ===============================================================
    // LISTAR TODOS
    // ===============================================================
    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }

    // ===============================================================
    // BUSCAR POR ID
    // ===============================================================
    public Livro buscarPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));
    }

    // ===============================================================
    // ATUALIZAR
    // ===============================================================
    @Transactional
    public Livro atualizar(Long id, Livro dados) {

        Livro livro = buscarPorId(id);

        livro.setTitulo(dados.getTitulo());
        livro.setAnoLancamento(dados.getAnoLancamento());
        livro.setFlagAtivo(dados.getFlagAtivo());
        livro.setSinopse(dados.getSinopse());

        // ======= STATUS ENTIDADE =======
        if (dados.getStatus() != null) {
            livro.setStatus(dados.getStatus());
        }

        // ======= AUTOR =======
        if (dados.getAutor() != null) {
            if (dados.getAutor().isBlank()) {
                livro.setAutores(null);
            } else {
                Autor autor = autorRepository.findByNomeIgnoreCase(dados.getAutor())
                        .orElseGet(() -> {
                            Autor novo = new Autor();
                            novo.setNome(dados.getAutor());
                            return autorRepository.save(novo);
                        });
                livro.setAutores(Set.of(autor));
            }
        }

        // ======= TEMA =======
        if (dados.getTema() != null) {
            if (dados.getTema().isBlank()) {
                livro.setTemas(null);
            } else {
                Tema tema = temaRepository.findByNomeIgnoreCase(dados.getTema())
                        .orElseGet(() -> {
                            Tema novo = new Tema();
                            novo.setNome(dados.getTema());
                            return temaRepository.save(novo);
                        });
                livro.setTemas(Set.of(tema));
            }
        }

        // ======= TAGS =======
        if (dados.getTags() != null) {
            if (dados.getTags().isEmpty()) {
                livro.setTagsEntidades(null);
            } else {
                var tags = dados.getTags().stream()
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(nome -> tagRepository.findByNomeIgnoreCase(nome)
                                .orElseGet(() -> {
                                    Tag t = new Tag();
                                    t.setNome(nome);
                                    return tagRepository.save(t);
                                })
                        )
                        .collect(java.util.stream.Collectors.toSet());
                livro.setTagsEntidades(tags);
            }
        }

        // ======= EDITORA =======
        if (dados.getEditora() != null) {
            if (dados.getEditora().isBlank()) {
                livro.setEditoraEntidade(null);
            } else {
                Editora editora = editoraRepository.findByNomeIgnoreCase(dados.getEditora())
                        .orElseGet(() -> {
                            Editora e = new Editora();
                            e.setNome(dados.getEditora());
                            return editoraRepository.save(e);
                        });
                livro.setEditoraEntidade(editora);
            }
        }

        return livroRepository.save(livro);
    }

    // ===============================================================
    // DELETAR
    // ===============================================================
    @Transactional
    public void deletar(Long id) {

        Livro livro = buscarPorId(id);

        boolean emprestado = emprestimoRepository.findAll().stream()
                .anyMatch(e -> e.getLivro().equals(livro)
                        && e.getStatus() != null
                        && e.getStatus().getNome().equalsIgnoreCase("ATIVO"));

        boolean reservado = reservaRepository.findAll().stream()
                .anyMatch(r -> r.getLivro().equals(livro)
                        && r.getStatus() != null
                        && r.getStatus().getNome().equalsIgnoreCase("ATIVA"));

        if (emprestado || reservado) {
            throw new RegraNegocioException(
                    "Não é possível excluir: o livro possui empréstimo ou reserva ativa."
            );
        }

        livroRepository.delete(livro);
    }

    // ===============================================================
    // CONSULTAR FILA DE RESERVA
    // ===============================================================
    public int consultarListaReserva(Livro livro) {
        List<Reserva> reservas = reservaRepository.findByLivroAndStatusOrderByDtInicioReservaAsc(
                livro,
                null // ajustado para modelo novo; pode trocar por status entidade depois
        );
        return reservas.size();
    }

    // ===============================================================
    // LISTAR COM BUSCA PAGINADA
    // ===============================================================
    public Page<Livro> listar(String q, Pageable pageable) {

        if (q == null || q.isBlank())
            return livroRepository.findAll(pageable);

        return livroRepository.searchByTituloAutorTemaTag(q, pageable);
    }
}
