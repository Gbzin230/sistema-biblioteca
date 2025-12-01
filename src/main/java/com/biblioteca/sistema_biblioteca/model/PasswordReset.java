package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_password_reset")
public class PasswordReset {

    @Id
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiration")
    private LocalDateTime expiracao;

    public PasswordReset() {}

    public PasswordReset(String email, String token, LocalDateTime expiracao) {
        this.email = email;
        this.token = token;
        this.expiracao = expiracao;
    }

    public String getEmail() { return email; }
    public String getToken() { return token; }
    public LocalDateTime getExpiracao() { return expiracao; }

    public void setEmail(String email) { this.email = email; }
    public void setToken(String token) { this.token = token; }
    public void setExpiracao(LocalDateTime expiracao) { this.expiracao = expiracao; }
}
