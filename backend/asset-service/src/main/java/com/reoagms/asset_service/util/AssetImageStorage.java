package com.reoagms.asset_service.util;

import com.reoagms.asset_service.common.exception.FileStorageException;
import com.reoagms.asset_service.common.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the on-disk side of asset image storage. Kept separate from the
 * service layer so the storage mechanism (local disk today, S3/blob storage
 * later) can be swapped without touching business logic.
 */
@Component
public class AssetImageStorage {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${app.file.asset-images-dir:uploads/asset-images}")
    private String uploadDir;

    public String store(MultipartFile file) {

        validate(file);

        Path root = Path.of(uploadDir);

        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize upload directory", e);
        }

        String extension = extractExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + extension;

        Path target = root.resolve(storedFileName).normalize();

        if (!target.startsWith(root.normalize())) {
            throw new FileStorageException("Invalid file path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store uploaded file", e);
        }

        return storedFileName;
    }

    public byte[] load(String storedFileName) {

        Path path = Path.of(uploadDir).resolve(storedFileName).normalize();

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read stored file: " + storedFileName, e);
        }

    }

    public void delete(String storedFileName) {

        Path path = Path.of(uploadDir).resolve(storedFileName).normalize();

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete stored file: " + storedFileName, e);
        }

    }

    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("Image file must not be empty");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidRequestException(
                    "Unsupported image type. Allowed types: " + List.copyOf(ALLOWED_CONTENT_TYPES));
        }

    }

    private String extractExtension(String originalFileName) {

        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }

        return originalFileName.substring(originalFileName.lastIndexOf('.'));

    }

}
