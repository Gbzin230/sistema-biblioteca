package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarEmail(
            @RequestParam String destinatario,
            @RequestParam String assunto,
            @RequestParam String corpo) {

        emailService.enviarEmail(destinatario, assunto, corpo);
        return ResponseEntity.ok("📨 E-mail enviado para " + destinatario);
    }
}
