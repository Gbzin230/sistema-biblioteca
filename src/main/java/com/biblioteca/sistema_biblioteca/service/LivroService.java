package com.biblioteca.sistema_biblioteca.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;
import com.biblioteca.sistema_biblioteca.dto.LivroRequestDTO;
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
    private final StatusEmprestimoRepository statusEmprestimoRepository;
    private final StatusReservaRepository statusReservaRepository;
    private final FileStorageService fileStorageService;
    private final ObraRepository obraRepository;

    private final String UPLOAD_DIR = "uploads/";

    public LivroService(LivroRepository livroRepository,
                        EmprestimoRepository emprestimoRepository,
                        ReservaRepository reservaRepository,
                        AutorRepository autorRepository,
                        TemaRepository temaRepository,
                        TagRepository tagRepository,
                        EditoraRepository editoraRepository,
                        StatusLivroRepository statusLivroRepository,
                        StatusEmprestimoRepository statusEmprestimoRepository,
                        StatusReservaRepository statusReservaRepository,
                        FileStorageService fileStorageService,
                        ObraRepository obraRepository) {

        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.autorRepository = autorRepository;
        this.temaRepository = temaRepository;
        this.tagRepository = tagRepository;
        this.editoraRepository = editoraRepository;
        this.statusLivroRepository = statusLivroRepository;
        this.statusEmprestimoRepository = statusEmprestimoRepository;
        this.statusReservaRepository = statusReservaRepository;
        this.fileStorageService = fileStorageService;
        this.obraRepository = obraRepository;
    }

    // ============================================================
    // CÁLCULO DO CAMPO DERIVADO
    // ============================================================
    public void preencherDisponibilidade(Livro livro) {

        StatusEmprestimo ativo =
                statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                        .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado"));

        long emprestados = emprestimoRepository.countByLivroAndStatusNomeIgnoreCase(livro, "ATIVO");

        int total = livro.getQuantidadeDisponivel() != null ? livro.getQuantidadeDisponivel() : 0;

        int disponiveis = Math.max(0, total - (int) emprestados);

        livro.setQuantidadeDisponivelEmprestar(disponiveis);

        // NÃO alteramos o status do livro aqui.
        // O status do livro é uma informação administrativa; disponibilidade é calculada por
        // quantidadeDisponivel - emprestimos_ativos. Evitamos mudar status automaticamente para EMPRESTADO.
    }

    // ===============================================================
    // SALVAR LIVRO
    // ===============================================================
    @Transactional
    public Livro salvar(Livro livro) {

        if (livro.getFlagAtivo() == null)
            livro.setFlagAtivo(Boolean.TRUE);

        if (livro.getStatus() == null) {
            StatusLivro disponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                    .orElseThrow(() -> new RegraNegocioException("Status 'DISPONIVEL' não existe."));
            livro.setStatus(disponivel);
        }

        // AUTOR
        if (livro.getAutor() != null && !livro.getAutor().isBlank()) {
            Autor autor = autorRepository.findByNomeIgnoreCase(livro.getAutor())
                    .orElseGet(() -> {
                        Autor novo = new Autor();
                        novo.setNome(livro.getAutor());
                        return autorRepository.save(novo);
                    });
            livro.setAutores(Set.of(autor));
        }

        // TEMA
        if (livro.getTema() != null && !livro.getTema().isBlank()) {
            Tema tema = temaRepository.findByNomeIgnoreCase(livro.getTema())
                    .orElseGet(() -> {
                        Tema novo = new Tema();
                        novo.setNome(livro.getTema());
                        return temaRepository.save(novo);
                    });
            livro.setTemas(Set.of(tema));
        }

        // TAGS
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

        // EDITORA
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
    // UPLOAD DE LIVRO
    // ===============================================================
    @Transactional
    public Livro criarComArquivos(LivroRequestDTO dto, MultipartFile capa, MultipartFile pdf) {

        Livro livro = new Livro();
        livro.setTitulo(dto.getTitulo());
        livro.setAnoLancamento(dto.getAnoLancamento());
        livro.setSinopse(dto.getSinopse());
        livro.setFlagAtivo(true);
        livro.setQuantidadeDisponivel(dto.getQuantidadeDisponivel());

        String validade = (dto.getDtValidade() == null || dto.getDtValidade().isBlank())
                ? "20401230"
                : dto.getDtValidade();
        livro.setDtValidade(validade);

        StatusLivro disponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status 'DISPONIVEL' não existe."));
        livro.setStatus(disponivel);

        if (dto.getEditora() != null && !dto.getEditora().isBlank()) {
            Editora editora = editoraRepository.findByNomeIgnoreCase(dto.getEditora())
                    .orElseGet(() -> {
                        Editora e = new Editora();
                        e.setNome(dto.getEditora());
                        return editoraRepository.save(e);
                    });
            livro.setEditoraEntidade(editora);
        }

        if (dto.getObra() != null && !dto.getObra().isBlank()) {
            Obra obra = obraRepository.findByNomeIgnoreCase(dto.getObra())
                    .orElseGet(() -> {
                        Obra o = new Obra();
                        o.setNome(dto.getObra());
                        return obraRepository.save(o);
                    });
            livro.setObraEntidade(obra);
        }

        if (dto.getAutor() != null && !dto.getAutor().isBlank()) {
            Autor autor = autorRepository.findByNomeIgnoreCase(dto.getAutor())
                    .orElseGet(() -> {
                        Autor novo = new Autor();
                        novo.setNome(dto.getAutor());
                        return autorRepository.save(novo);
                    });
            livro.setAutores(Set.of(autor));
        }

        if (dto.getTema() != null && !dto.getTema().isBlank()) {
            Tema tema = temaRepository.findByNomeIgnoreCase(dto.getTema())
                    .orElseGet(() -> {
                        Tema novo = new Tema();
                        novo.setNome(dto.getTema());
                        return temaRepository.save(novo);
                    });
            livro.setTemas(Set.of(tema));
        }

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            var tags = dto.getTags().stream()
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

        if (capa != null && !capa.isEmpty()) {
            String pathCapa = fileStorageService.salvarArquivo(capa, "capas");
            livro.setUriImgLivro(pathCapa);
        }

        if (pdf != null && !pdf.isEmpty()) {
            String pathPdf = fileStorageService.salvarArquivo(pdf, "pdfs");
            livro.setUriArquivoLivro(pathPdf);
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
    // LISTAR APENAS LIVROS ATIVOS
    // ===============================================================
    public Page<Livro> listarAtivos(Pageable pageable) {
        Page<Livro> page = livroRepository.findByFlagAtivoTrue(pageable);

        // Preenche disponibilidade para cada livro (sem alterar status)
        page.forEach(this::preencherDisponibilidade);

        return page;
    }

    // ===============================================================
    // BUSCAR POR ID
    // ===============================================================
    public Livro buscarPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));
    }

    public List<Livro> buscarPorTema(String tema) {
        return livroRepository.findByTemasNomeIgnoreCase(tema);
    }

    // ===============================================================
    // 🔥 NOVO: BUSCAR APENAS ATIVOS POR TEMA
    // ===============================================================
    public List<Livro> buscarPorTemaApenasAtivos(String tema) {
        return livroRepository.findByTemasNomeIgnoreCase(tema)
                .stream()
                .filter(Livro::getFlagAtivo)
                .peek(this::preencherDisponibilidade)
                .toList();
    }

    // ===============================================================
    // ATUALIZAR
    // ===============================================================
    @Transactional
    public Livro atualizar(Long id, Livro dados) {

        Livro livro = buscarPorId(id);

        if (dados.getTitulo() != null)
            livro.setTitulo(dados.getTitulo());

        if (dados.getAnoLancamento() != null)
            livro.setAnoLancamento(dados.getAnoLancamento());

        if (dados.getFlagAtivo() != null)
            livro.setFlagAtivo(dados.getFlagAtivo());

        if (dados.getSinopse() != null)
            livro.setSinopse(dados.getSinopse());

        if (dados.getQuantidadeDisponivel() != null) {

            int novoTotal = dados.getQuantidadeDisponivel();

            long emprestados = emprestimoRepository
                    .countByLivroAndStatusNomeIgnoreCase(livro, "ATIVO");

            if (novoTotal < emprestados) {
                throw new RegraNegocioException(
                        "Não é possível definir o total de licenças para " + novoTotal +
                        ". Existem " + emprestados + " empréstimos ativos."
                );
            }

            livro.setQuantidadeDisponivel(novoTotal);
        }

        if (dados.getDtValidade() != null && !dados.getDtValidade().isBlank())
            livro.setDtValidade(dados.getDtValidade());

        if (dados.getStatus() != null)
            livro.setStatus(dados.getStatus());

        if (dados.getObra() != null) {
            if (!dados.getObra().isBlank()) {
                Obra obra = obraRepository.findByNomeIgnoreCase(dados.getObra())
                        .orElseGet(() -> {
                            Obra nova = new Obra();
                            nova.setNome(dados.getObra());
                            return obraRepository.save(nova);
                        });
                livro.setObraEntidade(obra);
            }
        }

        if (dados.getAutor() != null && !dados.getAutor().isBlank()) {
            Autor autor = autorRepository.findByNomeIgnoreCase(dados.getAutor())
                    .orElseGet(() -> {
                        Autor novo = new Autor();
                        novo.setNome(dados.getAutor());
                        return autorRepository.save(novo);
                    });
            livro.setAutores(Set.of(autor));
        }

        if (dados.getTema() != null && !dados.getTema().isBlank()) {
            Tema tema = temaRepository.findByNomeIgnoreCase(dados.getTema())
                    .orElseGet(() -> {
                        Tema novo = new Tema();
                        novo.setNome(dados.getTema());
                        return temaRepository.save(novo);
                    });
            livro.setTemas(Set.of(tema));
        }

        if (dados.getTags() != null && !dados.getTags().isEmpty()) {
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

        preencherDisponibilidade(livro);

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
                null
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

    // ===============================================================
    // LISTAR COM BUSCA
    // ===============================================================
    public Page<Livro> buscarGlobal(String q, Pageable pageable) {
    if (q == null || q.isBlank()) {
        return livroRepository.findAll(pageable);
    }
    return livroRepository.searchGlobal(q.trim(), pageable)
            .map(livro -> {
                preencherDisponibilidade(livro);
                return livro;
            });
    }


    // ===============================================================
    // DESATIVAR LIVROS EM MASSA
    // ===============================================================
    @Transactional
    public int desativarLivrosEmMassa(List<Long> ids) {

        int count = 0;

        StatusLivro inativo = statusLivroRepository.findByNomeIgnoreCase("INATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status 'INATIVO' não existe."));

        StatusEmprestimo finalizado = statusEmprestimoRepository.findByNomeIgnoreCase("FINALIZADO")
                .orElseThrow(() -> new RegraNegocioException("Status 'FINALIZADO' para empréstimo não existe."));

        StatusReserva cancelado = statusReservaRepository.findByNomeIgnoreCase("CANCELADA")
                .orElseThrow(() -> new RegraNegocioException("Status 'CANCELADA' para reserva não existe."));

        LocalDateTime agora = LocalDateTime.now();

        for (Long id : ids) {
            try {
                Livro livro = buscarPorId(id);

                List<Emprestimo> emprestimosAtivos = emprestimoRepository.findAll().stream()
                        .filter(e -> e.getLivro().equals(livro)
                                && e.getStatus() != null
                                && e.getStatus().getNome().equalsIgnoreCase("ATIVO"))
                        .toList();

                for (Emprestimo e : emprestimosAtivos) {
                    e.setDtFim(agora);
                    e.encerrar(finalizado);
                    emprestimoRepository.save(e);
                }

                List<Reserva> reservasAtivas = reservaRepository.findAll().stream()
                        .filter(r -> r.getLivro().equals(livro)
                                && r.getStatus() != null
                                && r.getStatus().getNome().equalsIgnoreCase("ATIVA"))
                        .toList();

                for (Reserva r : reservasAtivas) {
                    r.setStatus(cancelado);
                    r.setDtFimReserva(null);
                    reservaRepository.save(r);
                }

                boolean emprestado = emprestimoRepository.findAll().stream()
                        .anyMatch(e -> e.getLivro().equals(livro)
                                && e.getStatus() != null
                                && e.getStatus().getNome().equalsIgnoreCase("ATIVO"));

                boolean reservado = reservaRepository.findAll().stream()
                        .anyMatch(r -> r.getLivro().equals(livro)
                                && r.getStatus() != null
                                && r.getStatus().getNome().equalsIgnoreCase("ATIVA"));

                if (emprestado || reservado) {
                    continue;
                }

                livro.setFlagAtivo(false);
                livro.setStatus(inativo);
                livroRepository.save(livro);
                count++;

            } catch (Exception ignored) {}
        }

        return count;
    }
}
