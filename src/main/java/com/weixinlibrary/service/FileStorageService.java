package com.weixinlibrary.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("txt", "epub", "pdf", "mobi");

    @Value("${app.file.upload-dir:./uploads/books}")
    private String uploadDir;

    public record StoredFile(String fileName, String fileUrl, long fileSize) {}

    public StoredFile store(MultipartFile file, String baseUrl) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("文件名无效");
        }

        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        String storedName = UUID.randomUUID() + "." + extension;
        Path targetPath = targetDir.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/api/files/books/" + storedName;

        log.info("Stored file {} as {}", originalName, storedName);
        return new StoredFile(storedName, fileUrl, file.getSize());
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new IllegalArgumentException("文件缺少扩展名");
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
