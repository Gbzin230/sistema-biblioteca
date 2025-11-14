package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.repository.EmprestimoRepository;
import com.biblioteca.sistema_biblioteca.repository.LivroRepository;
import com.biblioteca.sistema_biblioteca.repository.PessoaRepository;
import com.biblioteca.sistema_biblioteca.repository.UsuarioRepository;
import com.biblioteca.sistema_biblioteca.repository.ReservaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmprestimoServiceTest {

    private EmprestimoService service;
    private EmprestimoRepository emprestimoRepository;
    private LivroRepository livroRepository;
    private UsuarioRepository usuarioRepository;
    private ReservaRepository reservaRepository;
    private PessoaRepository pessoaRepository;

    @BeforeEach
    void setup() {
        emprestimoRepository = mock(EmprestimoRepository.class);
        livroRepository = mock(LivroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        pessoaRepository = mock(PessoaRepository.class);

        service = new EmprestimoService(
                emprestimoRepository,
                livroRepository,
                usuarioRepository,
                reservaRepository,
                pessoaRepository
        );
    }

    @Test
    void deveRealizarEmprestimoComSucesso() {

        Usuario usuario = new Usuario();
        usuario.setUsername("user123");
        usuario.setFlagAtivo(true);

        Livro livro = new Livro();
        livro.setId(1L);
        livro.setFlagAtivo(true);
        livro.alterarStatus(Livro.Status.DISPONIVEL);

        when(usuarioRepository.findById("user123"))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(1L))
                .thenReturn(Optional.of(livro));

        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = service.realizarEmprestimo("user123", 1L);

        assertNotNull(emprestimo);
        assertEquals(usuario, emprestimo.getUsuario());
        assertEquals(livro, emprestimo.getLivro());
        assertFalse(livro.isDisponivel());
    }

    @Test
    void deveLancarErroSeLivroNaoDisponivel() {

        Usuario usuario = new Usuario();
        usuario.setUsername("user123");
        usuario.setFlagAtivo(true);

        Livro livro = new Livro();
        livro.setId(1L);
        livro.setFlagAtivo(true);
        livro.alterarStatus(Livro.Status.EMPRESTADO); // já indisponível

        when(usuarioRepository.findById("user123"))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(1L))
                .thenReturn(Optional.of(livro));

        assertThrows(
                RegraNegocioException.class,
                () -> service.realizarEmprestimo("user123", 1L)
        );
    }

    @Test
    void deveRenovarEmprestimo() {

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(1L);
        emprestimo.setDtPrevistaDevolucao(java.time.LocalDate.now().plusDays(7));
        emprestimo.setNumRenovacoes(0);
        emprestimo.setStatus(Emprestimo.Status.ATIVO);

        when(emprestimoRepository.findById(1L))
                .thenReturn(Optional.of(emprestimo));

        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo renovado = service.renovarEmprestimo(1L);

        assertTrue(
                renovado.getDtPrevistaDevolucao()
                        .isAfter(java.time.LocalDate.now().plusDays(7))
        );

        assertEquals(1, renovado.getNumRenovacoes());
    }
}
