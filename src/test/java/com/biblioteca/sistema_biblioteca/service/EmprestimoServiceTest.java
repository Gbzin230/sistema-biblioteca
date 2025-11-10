package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.model.Livro;
import com.biblioteca.sistema_biblioteca.model.Pessoa;
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

    @BeforeEach
    void setup() {
        emprestimoRepository = mock(EmprestimoRepository.class);
        livroRepository = mock(LivroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        service = new EmprestimoService(emprestimoRepository, livroRepository, usuarioRepository, reservaRepository);
    }

    @Test
    void deveRealizarEmprestimoComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setFlagAtivo(true);

        Livro livro = new Livro();
        livro.setId(1L);
        livro.setFlagAtivo(true); // ✅ precisa estar ativo
        livro.alterarStatus(Livro.Status.DISPONIVEL);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.save(Mockito.any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = service.realizarEmprestimo(1L, 1L);

        assertNotNull(emprestimo);
        assertEquals(usuario, emprestimo.getUsuario());
        assertEquals(livro, emprestimo.getLivro());
        assertFalse(livro.isDisponivel());
    }

    @Test
    void deveLancarErroSeLivroNaoDisponivel() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setFlagAtivo(true);

        Livro livro = new Livro();
        livro.setId(1L);
        livro.setFlagAtivo(true); // ✅ ATIVAR PRIMEIRO
        livro.alterarStatus(Livro.Status.EMPRESTADO); // 🔹 Marca como indisponível

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        assertThrows(RegraNegocioException.class, () -> service.realizarEmprestimo(1L, 1L));
    }

    @Test
    void deveRenovarEmprestimo() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(1L);
        emprestimo.setDtPrevistaDevolucao(java.time.LocalDate.now().plusDays(7));

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(Mockito.any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo renovado = service.renovarEmprestimo(1L);
        assertTrue(renovado.getDtPrevistaDevolucao().isAfter(java.time.LocalDate.now().plusDays(7)));
    }
}
