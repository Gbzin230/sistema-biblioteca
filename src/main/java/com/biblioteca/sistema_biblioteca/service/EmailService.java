package com.biblioteca.sistema_biblioteca.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async // envia o e-mail em segundo plano
    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setText(mensagem);
            email.setFrom(remetente);

            mailSender.send(email);
            logger.info("📧 E-mail enviado para {}", destinatario);
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar e-mail para {}: {}", destinatario, e.getMessage());
        }
    }
}

