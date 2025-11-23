package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MetaDadosService {

    private final AutorRepository autorRepository;
    private final TemaRepository temaRepository;
    private final TagRepository tagRepository;
    private final EditoraRepository editoraRepository;
    private final ObraRepository obraRepository;

    public MetaDadosService(
            AutorRepository autorRepository,
            TemaRepository temaRepository,
            TagRepository tagRepository,
            EditoraRepository editoraRepository,
            ObraRepository obraRepository
    ) {
        this.autorRepository = autorRepository;
        this.temaRepository = temaRepository;
        this.tagRepository = tagRepository;
        this.editoraRepository = editoraRepository;
        this.obraRepository = obraRepository;
    }

    public Map<String, Object> buscarTodos() {

        Map<String, Object> resultado = new HashMap<>();

        resultado.put("autores", autorRepository.findAll());
        resultado.put("temas", temaRepository.findAll());
        resultado.put("tags", tagRepository.findAll());
        resultado.put("editoras", editoraRepository.findAll());
        resultado.put("obras", obraRepository.findAll());

        return resultado;
    }
}
