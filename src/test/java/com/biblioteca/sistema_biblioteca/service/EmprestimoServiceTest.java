package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
    private StatusLivroRepository statusLivroRepository;

    private StatusLivro statusDisponivel;
    private StatusLivro statusEmprestado;
    private StatusLivro statusReservado;

    @BeforeEach
    void setup() {

        // mocks
        emprestimoRepository = mock(EmprestimoRepository.class);
        livroRepository = mock(LivroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        pessoaRepository = mock(PessoaRepository.class);
        statusLivroRepository = mock(StatusLivroRepository.class);

        // Criar service com TODOS os 6 parâmetros do construtor atual
        service = new EmprestimoService(
                emprestimoRepository,
                livroRepository,
                usuarioRepository,
                reservaRepository,
                pessoaRepository,
                statusLivroRepository
        );

        // Criar status FAKE (não são enums!)
        statusDisponivel = new StatusLivro("DISPONIVEL");
        statusDisponivel.setId(1);

        statusEmprestado = new StatusLivro("EMPRESTADO");
        statusEmprestado.setId(2);

        statusReservado = new StatusLivro("RESERVADO");
        statusReservado.setId(3);

        // mockando retornos padrões do repository de status
        when(statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL"))
                .thenReturn(Optional.of(statusDisponivel));
        when(statusLivroRepository.findByNomeIgnoreCase("EMPRESTADO"))
                .thenReturn(Optional.of(statusEmprestado));
        when(statusLivroRepository.findByNomeIgnoreCase("RESERVADO"))
                .thenReturn(Optional.of(statusReservado));
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

        when(usuarioRepository.findById("user123"))
                .thenReturn(Optional.of(usuario));

        when(livroRepository.findById(1L))
                .thenReturn(Optional.of(livro));

        when(reservaRepository.countByUsuarioAndStatus(usuario, Reserva.ReservaStatus.ATIVA))
                .thenReturn(0);

        when(emprestimoRepository.countByUsuarioAndStatus(usuario, Emprestimo.Status.ATIVO))
                .thenReturn(0);

        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = service.realizarEmprestimo("user123", 1L);

        assertNotNull(emprestimo);
        assertEquals(usuario, emprestimo.getUsuario());
        assertEquals(livro, emprestimo.getLivro());
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
        emprestimo.setDtPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setNumRenovacoes(0);
        emprestimo.setStatus(Emprestimo.Status.ATIVO);

        when(emprestimoRepository.findById(1L))
                .thenReturn(Optional.of(emprestimo));

        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo renovado = service.renovarEmprestimo(1L);

        assertEquals(1, renovado.getNumRenovacoes());
        assertTrue(
                renovado.getDtPrevistaDevolucao()
                        .isAfter(LocalDate.now().plusDays(7))
        );
    }
}
