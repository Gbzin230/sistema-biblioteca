package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    Page<Livro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
    @Query("select l from Livro l join l.autores a where lower(a.nome) like lower(concat('%', :autor, '%'))")
    Page<Livro> findByAutorContainingIgnoreCase(@Param("autor") String autor, Pageable pageable);

    @Query("select l from Livro l left join l.autores a where lower(l.titulo) like lower(concat('%', :titulo, '%')) or lower(a.nome) like lower(concat('%', :autor, '%'))")
    Page<Livro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(@Param("titulo") String titulo, @Param("autor") String autor, Pageable pageable);

    @Query("select distinct l from Livro l join l.temas t where lower(t.nome) like lower(concat('%', :tema, '%'))")
    Page<Livro> findByTemaContainingIgnoreCase(@Param("tema") String tema, Pageable pageable);

    @Query("select distinct l from Livro l join l.tagsEntidades tg where lower(tg.nome) like lower(concat('%', :tag, '%'))")
    Page<Livro> findByTagContainingIgnoreCase(@Param("tag") String tag, Pageable pageable);

    @Query("select distinct l from Livro l left join l.autores a left join l.temas t left join l.tagsEntidades tg where lower(l.titulo) like lower(concat('%', :q, '%')) or lower(a.nome) like lower(concat('%', :q, '%')) or lower(t.nome) like lower(concat('%', :q, '%')) or lower(tg.nome) like lower(concat('%', :q, '%'))")
    Page<Livro> searchByTituloAutorTemaTag(@Param("q") String q, Pageable pageable);

    List<Livro> findByTemasNomeIgnoreCase(String nome);


    @Query(value = """
    SELECT 
        l.cod_livro,
        l.txt_titulo,
        l.ano_lancamento,
        l.num_total_licencas,
        l.dt_validade,
        l.flag_ativo,
        l.txt_sinopse,
        l.url_livro,
        l.uri_img_livro,

        e.nome_editora AS editora,
        o.nome_obra    AS obra,
        s.nome_status  AS status,

        GROUP_CONCAT(DISTINCT a.nome_autor SEPARATOR ', ') AS autores,
        GROUP_CONCAT(DISTINCT t.nome_tema SEPARATOR ', ') AS temas,
        GROUP_CONCAT(DISTINCT tg.nome_tag SEPARATOR ', ') AS tags

        FROM tb_livro l
        LEFT JOIN tb_editora e ON l.cod_editora = e.cod_editora
        LEFT JOIN tb_obra o ON l.cod_obra = o.cod_obra
        LEFT JOIN tb_status_livro s ON l.cod_status = s.cod_status

        LEFT JOIN tb_livro_autor la ON l.cod_livro = la.cod_livro
        LEFT JOIN tb_autor a ON la.cod_autor = a.cod_autor

        LEFT JOIN tb_livro_tema lt ON l.cod_livro = lt.cod_livro
        LEFT JOIN tb_tema t ON lt.cod_tema = t.cod_tema

        LEFT JOIN tb_livro_tags ltag ON l.cod_livro = ltag.cod_livro
        LEFT JOIN tb_tag tg ON ltag.cod_tag = tg.cod_tag

        WHERE l.cod_livro = :id
        GROUP BY l.cod_livro
    """,
    nativeQuery = true)
    Object buscarLivroCompleto(@Param("id") Long id);

}
