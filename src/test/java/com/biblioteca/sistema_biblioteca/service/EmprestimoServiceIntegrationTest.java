package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.dto.EmprestimoRequestDTO;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "joaosilva", roles = "USUARIO")
    void quandoCriarEmprestimo_entaoRetorna200() throws Exception {
        // 🔸 Cria o usuário/pessoa com o mesmo username do mock
        Usuario usuario = new Usuario();
        usuario.setNome("João da Silva");
        usuario.setEmail("joao@teste.com");
        usuario.setSenha("123456");
        usuario.setUsername("joaosilva"); // precisa bater com o @WithMockUser
        usuario.setFlagAtivo(true);
        usuario.setLimiteSlots(3);
        usuarioRepository.saveAndFlush(usuario);

        // 🔸 Cria livro disponível
        Livro livro = new Livro();
        livro.setTitulo("O Senhor dos Anéis");
        livro.setAutor("J.R.R. Tolkien");
        livro.setFlagAtivo(true);
        livro.setStatus(Livro.Status.DISPONIVEL);
        livroRepository.saveAndFlush(livro);

        // 🔸 DTO de requisição
        EmprestimoRequestDTO req = new EmprestimoRequestDTO();
        req.setLivroId(livro.getId());

        // 🔸 Executa requisição
        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.livroId", is(livro.getId().intValue())));
    }

}
