package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.PessoaRequestDTO;
import com.biblioteca.sistema_biblioteca.dto.PessoaResponseDTO;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public PessoaController(PessoaRepository pessoaRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    // ➕ Criar pessoa
    @PostMapping
    public ResponseEntity<PessoaResponseDTO> cadastrar(@Valid @RequestBody PessoaRequestDTO dto) {
        Usuario pessoa = modelMapper.map(dto, Usuario.class);
        pessoa.setSenha(passwordEncoder.encode(dto.getSenha()));

        Pessoa salva = pessoaRepository.save(pessoa);
        PessoaResponseDTO response = modelMapper.map(salva, PessoaResponseDTO.class);
        return ResponseEntity.ok(response);
    }

    // 📋 Listar pessoas
    @GetMapping
    public ResponseEntity<List<PessoaResponseDTO>> listar() {
        List<PessoaResponseDTO> pessoas = pessoaRepository.findAll()
                .stream()
                .map(p -> modelMapper.map(p, PessoaResponseDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pessoas);
    }

    // ❌ Deletar pessoa por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return pessoaRepository.findById(id)
                .map(pessoa -> {
                    pessoaRepository.delete(pessoa);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
