package com.biblioteca.sistema_biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import org.springframework.stereotype.Service;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public Livro salvarLivro(Livro livro) {
        return livroRepository.save(livro);
    }

    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }

    public Optional<Livro> buscaPorId(Long id) {
        return livroRepository.findById(id);
    }

    public void deletarLivro(Long id) {
        livroRepository.deleteById(id);
    }

}
