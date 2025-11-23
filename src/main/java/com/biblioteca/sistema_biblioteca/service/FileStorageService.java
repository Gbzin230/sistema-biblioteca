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

            String nomeArquivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path destino = basePath.resolve(subpasta).resolve(nomeArquivo);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return destino.toString(); // caminho completo no disco

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar arquivo: " + e.getMessage(), e);
        }
    }
}
