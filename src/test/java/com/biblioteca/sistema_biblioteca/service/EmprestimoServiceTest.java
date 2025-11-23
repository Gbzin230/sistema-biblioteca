package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EmprestimoServiceTest {

    private EmprestimoService service;
    private EmprestimoRepository emprestimoRepository;
    private LivroRepository livroRepository;
    private UsuarioRepository usuarioRepository;
    private ReservaRepository reservaRepository;
    private StatusLivroRepository statusLivroRepository;
    private StatusEmprestimoRepository statusEmprestimoRepository;
    private StatusReservaRepository statusReservaRepository;

    private StatusLivro statusDisponivel;
    private StatusLivro statusEmprestado;
    private StatusLivro statusReservado;

    private StatusEmprestimo statusAtivo;
    private StatusEmprestimo statusFinalizado;

    @BeforeEach
        void setup() {

        emprestimoRepository = mock(EmprestimoRepository.class);
        livroRepository = mock(LivroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        statusLivroRepository = mock(StatusLivroRepository.class);
        statusEmprestimoRepository = mock(StatusEmprestimoRepository.class);
        statusReservaRepository = mock(StatusReservaRepository.class);

        service = new EmprestimoService(
                emprestimoRepository,
                livroRepository,
                usuarioRepository,
                reservaRepository,
                statusLivroRepository,
                statusEmprestimoRepository,
                statusReservaRepository
        );

        statusDisponivel = new StatusLivro("DISPONIVEL");
        statusDisponivel.setId(1);

        statusEmprestado = new StatusLivro("EMPRESTADO");
        statusEmprestado.setId(2);

        statusReservado = new StatusLivro("RESERVADO");
        statusReservado.setId(3);

        when(statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL"))
                .thenReturn(Optional.of(statusDisponivel));
        when(statusLivroRepository.findByNomeIgnoreCase("EMPRESTADO"))
                .thenReturn(Optional.of(statusEmprestado));
        when(statusLivroRepository.findByNomeIgnoreCase("RESERVADO"))
                .thenReturn(Optional.of(statusReservado));

        StatusEmprestimo ativo = new StatusEmprestimo();
        ativo.setId(1);
        ativo.setNome("ATIVO");

        when(statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO"))
                .thenReturn(Optional.of(ativo));

        StatusReserva ativa = new StatusReserva();
        ativa.setId(1);
        ativa.setNome("ATIVA");

        when(statusReservaRepository.findByNomeIgnoreCase("ATIVA"))
                .thenReturn(Optional.of(ativa));
        }


    @Test
    void deveRealizarEmprestimoComSucesso() {

        Usuario usuario = new Usuario();
        usuario.setUsername("user123");
        usuario.setFlagAtivo(true);

        Livro livro = new Livro();
        livro.setId(1L);
        livro.setFlagAtivo(true);
        livro.setStatus(statusDisponivel);

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        when(reservaRepository.countByUsuarioAndStatus(eq(usuario), any())).thenReturn(0);
        when(emprestimoRepository.countByUsuarioAndStatus(usuario, statusAtivo)).thenReturn(0);

        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Emprestimo e = service.realizarEmprestimo("user123", 1L);

        assertNotNull(e);
        assertEquals(usuario, e.getUsuario());
        assertEquals(livro, e.getLivro());
        assertEquals("EMPRESTADO", livro.getStatus().getNome());
    }

    @Test
    void deveLancarErroSeLivroNaoDisponivel() {

        Usuario usuario = new Usuario();
        usuario.setUsername("user123");
        usuario.setFlagAtivo(true);

        Livro livro = new Livro();
        livro.setId(1L);
        livro.setFlagAtivo(true);
        livro.setStatus(statusEmprestado);

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        assertThrows(RegraNegocioException.class,
                () -> service.realizarEmprestimo("user123", 1L));
    }

    @Test
    void deveRenovarEmprestimo() {

        Emprestimo e = new Emprestimo();
        e.setId(1L);
        e.setStatus(statusAtivo);
        e.setNumRenovacoes(0);
        e.setDtFim(LocalDateTime.now().plusDays(7));

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(e));
        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Emprestimo renovado = service.renovarEmprestimo(1L);

        assertEquals(1, renovado.getNumRenovacoes());
        assertTrue(renovado.getDtFim().isAfter(e.getDtFim()));
    }
}
