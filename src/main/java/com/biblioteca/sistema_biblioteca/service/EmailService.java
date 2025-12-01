package com.biblioteca.sistema_biblioteca.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarEmail(String destinatario, String assunto, String mensagemHtml) {
        try {

            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(assunto);

            // 👉 AGORA É HTML DE VERDADE
            helper.setText(mensagemHtml, true);

            helper.setFrom(remetente);

            mailSender.send(mensagem);
            logger.info("📧 E-mail enviado para {}", destinatario);

        } catch (Exception e) {
            logger.error("❌ Erro ao enviar e-mail para {}: {}", destinatario, e.getMessage());
        }
    }
}
