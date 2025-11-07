package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.model.Emprestimo;
import com.biblioteca.sistema_biblioteca.repository.EmprestimoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.List;

@Component
@Transactional
@ConditionalOnProperty(prefix = "spring.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmprestimoScheduler {

    private final EmprestimoRepository emprestimoRepository;
    private final EmprestimoService emprestimoService;

    public EmprestimoScheduler(EmprestimoRepository emprestimoRepository, EmprestimoService emprestimoService) {
        this.emprestimoRepository = emprestimoRepository;
        this.emprestimoService = emprestimoService;
    }

    // Executa todo dia à meia-noite
    @Scheduled(cron = "0 0 0 * * *")
    public void devolverEmprestimosVencidos() {
        LocalDate hoje = LocalDate.now();

        List<Emprestimo> emprestimosAtivos = emprestimoRepository.findAll()
                .stream()
                .filter(e -> "ATIVO".equals(e.getStatus()))
                .filter(e -> e.getDtPrevistaDevolucao().isBefore(hoje))
                .toList();

        for (Emprestimo emprestimo : emprestimosAtivos) {
            try {
                emprestimoService.devolverLivro(emprestimo.getId());
                System.out.printf("✅ Empréstimo %d devolvido automaticamente (vencido em %s)%n",
                        emprestimo.getId(), emprestimo.getDtPrevistaDevolucao());
            } catch (Exception e) {
                System.err.printf("❌ Erro ao devolver empréstimo %d: %s%n",
                        emprestimo.getId(), e.getMessage());
            }
        }

        if (!emprestimosAtivos.isEmpty()) {
            System.out.println("📊 Total de devoluções automáticas: " + emprestimosAtivos.size());
        }
    }
}
