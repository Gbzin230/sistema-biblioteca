package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.dto.EmprestimoRequestDTO;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.biblioteca.sistema_biblioteca.SistemaBibliotecaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmprestimoServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StatusLivroRepository statusLivroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();
        statusLivroRepository.deleteAll();
        autorRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "joaosilva", roles = "USUARIO")
    void quandoCriarEmprestimo_entaoRetorna200() throws Exception {

        // 🔹 Cria status DISPONIVEL
        StatusLivro statusDisponivel = new StatusLivro();
        statusDisponivel.setNome("DISPONIVEL");
        statusLivroRepository.saveAndFlush(statusDisponivel);

        // 🔹 Cria o usuário
        Usuario usuario = new Usuario();
        usuario.setNome("João da Silva");
        usuario.setEmail("joao@teste.com");
        usuario.setSenha("123456");
        usuario.setUsername("joaosilva");
        usuario.setFlagAtivo(true);
        usuario.setLimiteSlots(3);
        usuarioRepository.saveAndFlush(usuario);

        // 🔹 Cria autor (pois Livro agora usa relacionamento)
        Autor autor = new Autor();
        autor.setNome("J.R.R. Tolkien");
        autor = autorRepository.saveAndFlush(autor);

        // 🔹 Cria livro disponível
        Livro livro = new Livro();
        livro.setTitulo("O Senhor dos Anéis");
        livro.setAutores(Set.of(autor)); // ✔ necessário agora
        livro.setFlagAtivo(true);
        livro.setStatus(statusDisponivel); // ✔ entidade real persistida

        livroRepository.saveAndFlush(livro);

        // 🔹 DTO
        EmprestimoRequestDTO req = new EmprestimoRequestDTO();
        req.setLivroId(livro.getId());

        // 🔹 Executa requisição
        mockMvc.perform(
                post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id", notNullValue()))
        .andExpect(jsonPath("$.data.livroId", is(livro.getId().intValue())));
    }
}
