package com.biblioteca.sistema_biblioteca.repository;

import com.biblioteca.sistema_biblioteca.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
