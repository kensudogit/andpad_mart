package jp.andpad.api.bim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BimAssetStorage {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml");

    private final Path uploadRoot;
    private final long maxImageBytes;

    public BimAssetStorage(
            @Value("${app.bim.upload-dir:uploads/bim}") String uploadDir,
            @Value("${app.bim.max-image-bytes:5242880}") long maxImageBytes) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.maxImageBytes = maxImageBytes;
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("cannot create BIM upload directory: " + this.uploadRoot, ex);
        }
    }

    public StoredBimAsset storeImage(String orgId, String originalFileName, String contentType, byte[] bytes) {
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId is required");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("file is empty");
        }
        if (bytes.length > maxImageBytes) {
            throw new IllegalArgumentException("image exceeds max size (" + maxImageBytes + " bytes)");
        }
        String normalizedType = normalizeContentType(contentType, originalFileName);
        if (!ALLOWED_IMAGE_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("unsupported image type: " + normalizedType);
        }

        String extension = extensionFor(normalizedType, originalFileName);
        String storedName = UUID.randomUUID() + extension;
        Path orgDir = uploadRoot.resolve(sanitizeOrgId(orgId));
        Path target = orgDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("invalid upload path");
        }
        try {
            Files.createDirectories(orgDir);
            Files.write(target, bytes);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to store BIM image", ex);
        }

        String publicUrl = "/api/saas/bim/files/" + sanitizeOrgId(orgId) + "/" + storedName;
        log.info("stored BIM image orgId={} name={} bytes={}", orgId, storedName, bytes.length);
        return new StoredBimAsset(publicUrl, storedName, normalizedType, bytes.length);
    }

    public Resource load(String orgId, String fileName) throws IOException {
        Path file = resolveStoredFile(orgId, fileName);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        return new FileSystemResource(file);
    }

    public String contentTypeFor(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "image/jpeg";
    }

    private Path resolveStoredFile(String orgId, String fileName) {
        String safeName = sanitizeFileName(fileName);
        Path file = uploadRoot.resolve(sanitizeOrgId(orgId)).resolve(safeName).normalize();
        if (!file.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("invalid file path");
        }
        return file;
    }

    private static String sanitizeOrgId(String orgId) {
        return orgId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private static String sanitizeFileName(String fileName) {
        String base = StringUtils.getFilename(fileName);
        if (base == null || base.isBlank()) {
            throw new IllegalArgumentException("invalid file name");
        }
        return base.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    private static String normalizeContentType(String contentType, String originalFileName) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        }
        String lower = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private static String extensionFor(String contentType, String originalFileName) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/svg+xml" -> ".svg";
            default -> {
                String lower = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".png")) yield ".png";
                if (lower.endsWith(".webp")) yield ".webp";
                if (lower.endsWith(".gif")) yield ".gif";
                if (lower.endsWith(".svg")) yield ".svg";
                yield ".jpg";
            }
        };
    }

    public record StoredBimAsset(String url, String storedName, String contentType, long sizeBytes) {}
}
