package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.*;
import com.biblioteca.sistema_biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final StatusLivroRepository statusLivroRepository;
    private final StatusEmprestimoRepository statusEmprestimoRepository;
    private final StatusReservaRepository statusReservaRepository;
    private final LivroService livroService;

    private static final int PRAZO_PADRAO_DIAS = 7;
    private static final int MAX_RENOVACOES = 2;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository,
            StatusLivroRepository statusLivroRepository,
            StatusEmprestimoRepository statusEmprestimoRepository,
            StatusReservaRepository statusReservaRepository,
            LivroService livroService
    ) {

        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.statusLivroRepository = statusLivroRepository;
        this.statusEmprestimoRepository = statusEmprestimoRepository;
        this.statusReservaRepository = statusReservaRepository;
        this.livroService = livroService;
    }

    // ====================================================================
    // REALIZAR EMPRÉSTIMO
    // ====================================================================
    @Transactional
    public Emprestimo realizarEmprestimo(String usuarioId, Long livroId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RegraNegocioException("Livro não encontrado."));

        StatusEmprestimo statusAtivo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVA não encontrado."));

        int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(usuario, statusAtivo);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(usuario, ativa);

        // =====================================================
        // 🔥 RESPEITA O LIMITE REAL DO USUÁRIO
        // =====================================================
        int limite = usuario.getLimiteSlots() != null ? usuario.getLimiteSlots() : 3;

        if (emprestimosAtivos + reservasAtivas >= limite) {
            throw new RegraNegocioException("Limite de slots atingido: " + limite);
        }

        if (!usuario.getFlagAtivo()) {
            throw new RegraNegocioException("Usuário bloqueado não pode realizar empréstimos.");
        }

        // =====================================================
        // 🔥 VALIDAÇÃO CORRETA DE DISPONIBILIDADE
        // =====================================================
        livroService.preencherDisponibilidade(livro);

        int disponivel = livro.getQuantidadeDisponivelEmprestar() != null
                ? livro.getQuantidadeDisponivelEmprestar()
                : 0;

        if (disponivel <= 0) {
            throw new RegraNegocioException("Nenhuma unidade disponível para empréstimo.");
        }

        LocalDateTime agora = LocalDateTime.now(ZONE);

        Emprestimo emprestimo = new Emprestimo(usuario, livro);
        emprestimo.setDtInicio(agora);
        emprestimo.setDtFim(agora.plusDays(PRAZO_PADRAO_DIAS));
        emprestimo.setStatus(statusAtivo);

        // NÃO alteramos status do livro
        return emprestimoRepository.save(emprestimo);
    }

    // ====================================================================
    // RENOVAR EMPRÉSTIMO
    // ====================================================================
    @Transactional
    public Emprestimo renovarEmprestimo(Long emprestimoId) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));

        if (!emprestimo.getStatus().equals(ativo)) {
            throw new RegraNegocioException("Apenas empréstimos ativos podem ser renovados.");
        }

        if (emprestimo.getNumRenovacoes() >= MAX_RENOVACOES) {
            throw new RegraNegocioException("Máximo de renovações atingido.");
        }

        emprestimo.setNumRenovacoes(emprestimo.getNumRenovacoes() + 1);
        emprestimo.setDtFim(emprestimo.getDtFim().plusDays(PRAZO_PADRAO_DIAS));

        return emprestimoRepository.save(emprestimo);
    }

    // ====================================================================
    // DEVOLVER LIVRO
    // ====================================================================
    @Transactional
    public void devolverLivro(Long emprestimoId) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        StatusEmprestimo finalizado = statusEmprestimoRepository.findByNomeIgnoreCase("FINALIZADO")
                .orElseThrow(() -> new RegraNegocioException("Status FINALIZADO não encontrado."));

        emprestimo.setStatus(finalizado);
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();
        if (livro == null) return;

        StatusLivro disponivel = statusLivroRepository.findByNomeIgnoreCase("DISPONIVEL")
                .orElseThrow(() -> new RegraNegocioException("Status DISPONIVEL não existe."));

        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não existe."));

        StatusReserva ativa = statusReservaRepository.findByNomeIgnoreCase("ATIVA")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVA não existe."));

        StatusReserva finalizadaReserva = statusReservaRepository.findByNomeIgnoreCase("FINALIZADA")
                .orElseThrow(() -> new RegraNegocioException("Status FINALIZADA não existe."));

        List<Reserva> fila = reservaRepository
                .findByLivroAndStatusOrderByDtInicioReservaAsc(livro, ativa);

        if (fila.isEmpty()) {
            livro.setStatus(disponivel);
            livroRepository.save(livro);
            return;
        }

        Reserva reserva = fila.get(0);
        Usuario user = reserva.getUsuario();

        int emprestimosAtivos = emprestimoRepository.countByUsuarioAndStatus(user, ativo);
        int reservasAtivas = reservaRepository.countByUsuarioAndStatus(user, ativa);

        int limite = user.getLimiteSlots() != null ? user.getLimiteSlots() : 3;

        if (!user.getFlagAtivo() || emprestimosAtivos + reservasAtivas >= limite) {

            reserva.setStatus(finalizadaReserva);
            reservaRepository.save(reserva);

            fila.remove(0);
            if (!fila.isEmpty()) {
                devolverLivro(emprestimoId);
            } else {
                livro.setStatus(disponivel);
                livroRepository.save(livro);
            }
            return;
        }

        LocalDateTime agora = LocalDateTime.now(ZONE);

        Emprestimo novo = new Emprestimo(user, livro);
        novo.setDtInicio(agora);
        novo.setDtFim(agora.plusDays(PRAZO_PADRAO_DIAS));
        novo.setStatus(ativo);
        emprestimoRepository.save(novo);

        reserva.setStatus(finalizadaReserva);
        reservaRepository.save(reserva);
    }

    // ====================================================================
    // CONSULTAS
    // ====================================================================
    @Transactional(readOnly = true)
    public Emprestimo buscarPorId(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarEmprestimos() {
        return emprestimoRepository.findAll();
    }

    public int countEmprestimosAtivos(Usuario usuario) {
        StatusEmprestimo ativo = statusEmprestimoRepository.findByNomeIgnoreCase("ATIVO")
                .orElseThrow(() -> new RegraNegocioException("Status ATIVO não encontrado."));
        return emprestimoRepository.countByUsuarioAndStatus(usuario, ativo);
    }

    // ====================================================================
    // CONSULTAR EMPRÉSTIMOS DE UM USUÁRIO
    // ====================================================================
    @Transactional(readOnly = true)
    public List<Emprestimo> consultarEmprestimosUsuario(String username, String authUser, boolean isAdminOrFuncionario) {

        if (!isAdminOrFuncionario && !username.equals(authUser)) {
            throw new AccessDeniedException("Você só pode consultar seus próprios empréstimos.");
        }

        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        return emprestimoRepository.findByUsuario(usuario);
    }

    // ====================================================================
    // HISTÓRICO
    // ====================================================================
    @Transactional(readOnly = true)
    public List<Emprestimo> buscarHistorico(String usernameConsulta, String usernameAuth) {

        Usuario authUser = usuarioRepository.findById(usernameAuth)
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));

        Usuario alvo = usuarioRepository.findById(usernameConsulta)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = authUser.getRole().getNome();

        if (role.equalsIgnoreCase("USUARIO") &&
                !authUser.getUsername().equals(alvo.getUsername())) {

            throw new AccessDeniedException("Você não pode consultar o histórico de outro usuário.");
        }

        return emprestimoRepository.findByUsuario(alvo);
    }

    // ====================================================================
    // SEGURANÇA
    // ====================================================================
    public void validarDonoDoEmprestimo(Long emprestimoId, String username) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        if (!emprestimo.getUsuario().getUsername().equals(username)) {
            throw new AccessDeniedException("Você não pode acessar empréstimos de outro usuário.");
        }
    }

    public void validarUsuarioEmprestimo(String usuarioId, String username) {

        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        if (!usuario.getUsername().equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode realizar empréstimos em seu próprio nome.");
        }
    }

    public void devolverAutorizado(Long emprestimoId, String username) {

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraNegocioException("Empréstimo não encontrado."));

        Usuario usuario = usuarioRepository.findById(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String role = usuario.getRole().getNome();

        if (role.equalsIgnoreCase("USUARIO") &&
                !emprestimo.getUsuario().getUsername().equals(username)) {

            throw new AccessDeniedException("Você não pode devolver empréstimos de outro usuário.");
        }

        devolverLivro(emprestimoId);
    }
}
