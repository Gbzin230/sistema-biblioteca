package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.repository.EmprestimoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component
@Transactional
public class EmprestimoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EmprestimoScheduler.class);
    private final EmprestimoRepository emprestimoRepository;
    private final EmprestimoService emprestimoService;
    private final EmailService emailService;

    public EmprestimoScheduler(EmprestimoRepository emprestimoRepository,
                               EmprestimoService emprestimoService,
                               EmailService emailService) {
        this.emprestimoRepository = emprestimoRepository;
        this.emprestimoService = emprestimoService;
        this.emailService = emailService;
    }

    // Executa todo dia às 6h da manhã
    @Scheduled(cron = "0 0 6 * * *")
    public void verificarEmprestimos() {
        LocalDate hoje = LocalDate.now();

        // Busca só os empréstimos ativos e com data próxima
        List<Emprestimo> emprestimos = emprestimoRepository.findAll().stream()
                .filter(e -> "ATIVO".equals(e.getStatus()))
                .toList();

        for (Emprestimo e : emprestimos) {
            LocalDate devolucao = e.getDtPrevistaDevolucao();

            if (devolucao.equals(hoje.plusDays(1))) {
                // Um dia antes do vencimento
                emailService.enviarEmail(
                        e.getUsuario().getEmail(),
                        "📘 Lembrete: devolução do livro amanhã!",
                        "Olá " + e.getUsuario().getNome() + ",\n\n"
                                + "O livro \"" + e.getLivro().getTitulo() + "\" deve ser devolvido até amanhã (" + devolucao + ").\n"
                                + "Evite multas devolvendo dentro do prazo.\n\nBiblioteca Municipal"
                );
            } else if (devolucao.isBefore(hoje)) {
                // Já venceu → devolve automaticamente e avisa
                emprestimoService.devolverLivro(e.getId());
                emailService.enviarEmail(
                        e.getUsuario().getEmail(),
                        "⚠️ Livro devolvido automaticamente",
                        "Olá " + e.getUsuario().getNome() + ",\n\n"
                                + "O livro \"" + e.getLivro().getTitulo() + "\" foi devolvido automaticamente, pois o prazo venceu em "
                                + devolucao + ".\n"
                                + "Entre em contato com a biblioteca caso tenha dúvidas.\n\nBiblioteca Municipal"
                );
                logger.info("📊 Empréstimo {} devolvido automaticamente (vencido em {})",
                        e.getId(), devolucao);
            }
        }
    }
}

