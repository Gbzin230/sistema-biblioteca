package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
@RestController
@RequestMapping("/teste-email")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<String> teste() {
        emailService.enviarEmail(
                "gui.svalerio2005@gmail.com",
                "📚 Teste de e-mail",
                "Se você recebeu este e-mail, o envio via Gmail está funcionando!"
        );
        return ResponseEntity.ok("E-mail enviado com sucesso!");
    }
}