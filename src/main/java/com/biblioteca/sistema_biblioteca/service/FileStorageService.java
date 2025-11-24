package com.biblioteca.sistema_biblioteca.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path basePath;

    public FileStorageService(@Value("${biblioteca.upload.dir}") String uploadDir) {
        this.basePath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // cria a pasta raiz /uploads
            Files.createDirectories(basePath);

            // cria as subpastas necessárias
            Files.createDirectories(basePath.resolve("capas"));
            Files.createDirectories(basePath.resolve("pdfs"));

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar estrutura de diretórios para upload", e);
        }
    }

    public String salvarArquivo(MultipartFile file, String subpasta) {
        try {
            if (file == null || file.isEmpty()) return null;

            // pasta absoluta (usando config ou propriedades)
            String uploadsBase = "C:/Users/astronyx/Documents/sistema-biblioteca/uploads"; // ou injete via @Value("${biblioteca.upload.dir}")
            Path pastaPath = Paths.get(uploadsBase, subpasta);
            Files.createDirectories(pastaPath);

            String nome = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path destino = pastaPath.resolve(nome);
            file.transferTo(destino.toFile());

            // Retorna caminho relativo usado pelo frontend (/uploads/...)
            return "/uploads/" + subpasta + "/" + nome;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar arquivo: " + e.getMessage(), e);
        }
    }

}
