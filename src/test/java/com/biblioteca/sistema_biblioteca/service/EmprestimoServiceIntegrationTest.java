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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.biblioteca.sistema_biblioteca.SistemaBibliotecaApplication.class)
@AutoConfigureMockMvc(addFilters = false)
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

    // Mock para não precisar do scheduler real
    @MockBean
    private EmprestimoScheduler emprestimoScheduler;

    @BeforeEach
    void setup() {
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void quandoCriarEmprestimo_entaoRetorna200() throws Exception {
        // 🔸 Cria usuário ativo
        Usuario usuario = new Usuario();
        usuario.setNome("João da Silva");
        usuario.setEmail("joao@teste.com");
        usuario.setSenha("123456");
        usuario.setUsername("joaosilva");
        usuario.setFlagAtivo(true);
        usuario.setLimiteSlots(3);
        usuarioRepository.saveAndFlush(usuario);

        Usuario loaded = usuarioRepository.findById(usuario.getId()).orElseThrow();
        System.out.println(">>> FLAG NO BANCO: " + loaded.isFlagAtivo());

        // 🔸 Cria livro disponível
        Livro livro = new Livro();
        livro.setTitulo("O Senhor dos Anéis");
        livro.setAutor("J.R.R. Tolkien");
        livro.setFlagAtivo(true);
        livro.setStatus(Livro.Status.DISPONIVEL);
        livro = livroRepository.save(livro);

        // 🔸 Cria DTO de requisição
        EmprestimoRequestDTO req = new EmprestimoRequestDTO();
        req.setUsuarioId(usuario.getId());
        req.setLivroId(livro.getId());

        // 🔸 Faz requisição e valida resposta
        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.usuarioId", is(usuario.getId().intValue())))
                .andExpect(jsonPath("$.data.livroId", is(livro.getId().intValue())));
    }
}
