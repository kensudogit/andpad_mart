package jp.andpad.api.bim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.andpad.api.repository.BimFileRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BimAssetStorage {

    public static final String KIND_THUMBNAIL = "THUMBNAIL";
    public static final String KIND_MODEL = "MODEL";

    private static final Set<String> ALLOWED_MODEL_TYPES = Set.of(
            "model/gltf-binary",
            "model/gltf+json",
            "model/gltf",
            "application/octet-stream",
            "application/json",
            "text/plain");

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml");

    private final BimFileRepository bimFileRepository;
    private final Path uploadRoot;
    private final long maxImageBytes;
    private final long maxModelBytes;

    @Autowired
    public BimAssetStorage(
            BimFileRepository bimFileRepository,
            @Value("${app.bim.upload-dir:uploads/bim}") String uploadDir,
            @Value("${app.bim.max-image-bytes:5242880}") long maxImageBytes,
            @Value("${app.bim.max-model-bytes:52428800}") long maxModelBytes) {
        this.bimFileRepository = bimFileRepository;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.maxImageBytes = maxImageBytes;
        this.maxModelBytes = maxModelBytes;
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("cannot create BIM upload directory: " + this.uploadRoot, ex);
        }
    }

    public StoredBimAsset storeImage(String orgId, String originalFileName, String contentType, byte[] bytes) {
        return store(orgId, originalFileName, contentType, bytes, KIND_THUMBNAIL, maxImageBytes);
    }

    public StoredBimAsset storeModel(String orgId, String originalFileName, String contentType, byte[] bytes) {
        return store(orgId, originalFileName, contentType, bytes, KIND_MODEL, maxModelBytes);
    }

    private StoredBimAsset store(
            String orgId,
            String originalFileName,
            String contentType,
            byte[] bytes,
            String fileKind,
            long maxBytes) {
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId is required");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("file is empty");
        }
        if (bytes.length > maxBytes) {
            throw new IllegalArgumentException("file exceeds max size (" + maxBytes + " bytes)");
        }

        String normalizedType = normalizeContentType(contentType, originalFileName, fileKind, bytes);
        if (!isAllowedContent(normalizedType, originalFileName, fileKind, bytes)) {
            if (KIND_MODEL.equals(fileKind)) {
                throw new IllegalArgumentException(
                        "unsupported 3D model type: " + normalizedType + " (use .glb or .gltf)");
            }
            if (isModelContentType(normalizedType) || looksLikeModelBytes(bytes)) {
                throw new IllegalArgumentException(
                        "GLB/GLTF files must be uploaded with the 3D model button, not thumbnail");
            }
            throw new IllegalArgumentException("unsupported file type: " + normalizedType);
        }

        if (KIND_MODEL.equals(fileKind)) {
            assertGltfSelfContained(bytes);
        }

        String extension = extensionFor(normalizedType, originalFileName, fileKind);
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
            throw new IllegalStateException("failed to store BIM file", ex);
        }

        try {
            bimFileRepository.save(orgId, storedName, fileKind, normalizedType, bytes);
        } catch (Exception ex) {
            log.warn(
                    "BIM file saved on disk but database persist failed (orgId={} name={}): {}",
                    orgId,
                    storedName,
                    ex.getMessage());
        }

        String publicUrl = "/api/saas/bim/files/" + sanitizeOrgId(orgId) + "/" + storedName;
        log.info("stored BIM {} orgId={} name={} bytes={}", fileKind, orgId, storedName, bytes.length);
        return new StoredBimAsset(publicUrl, storedName, normalizedType, bytes.length);
    }

    public LoadedBimAsset load(String orgId, String fileName) throws IOException {
        Path file = resolveStoredFile(orgId, fileName);
        if (Files.isRegularFile(file)) {
            return new LoadedBimAsset(new FileSystemResource(file), contentTypeFor(fileName));
        }
        return bimFileRepository
                .find(orgId, sanitizeFileName(fileName))
                .map(bytes -> new LoadedBimAsset(new ByteArrayResource(bytes), contentTypeFor(fileName)))
                .orElse(null);
    }

    public String contentTypeFor(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".glb")) return "model/gltf-binary";
        if (lower.endsWith(".gltf")) return "model/gltf+json";
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

    private static boolean isAllowedContent(
            String normalizedType, String originalFileName, String fileKind, byte[] bytes) {
        if (KIND_MODEL.equals(fileKind)) {
            if (ALLOWED_MODEL_TYPES.contains(normalizedType)) {
                return true;
            }
            if (isModelContentType(normalizedType)) {
                return true;
            }
            if (isAllowedByExtension(originalFileName, fileKind)) {
                return true;
            }
            return looksLikeModelBytes(bytes);
        }
        if (ALLOWED_IMAGE_TYPES.contains(normalizedType)) {
            return true;
        }
        return isAllowedByExtension(originalFileName, fileKind);
    }

    private static boolean isModelContentType(String normalizedType) {
        return normalizedType.startsWith("model/gltf");
    }

    private static boolean looksLikeGltfJson(byte[] bytes) {
        for (int i = 0; i < Math.min(bytes.length, 256); i++) {
            byte b = bytes[i];
            if (!Character.isWhitespace(b)) {
                return b == '{';
            }
        }
        return false;
    }

    private static void assertGltfSelfContained(byte[] bytes) {
        if (!looksLikeGltfJson(bytes)) {
            return;
        }
        try {
            JsonNode root = new ObjectMapper().readTree(bytes);
            JsonNode buffers = root.get("buffers");
            if (buffers == null || !buffers.isArray()) {
                return;
            }
            for (JsonNode buffer : buffers) {
                JsonNode uri = buffer.get("uri");
                if (uri != null && uri.isTextual()) {
                    String value = uri.asText("");
                    if (!value.isBlank() && !value.startsWith("data:")) {
                        throw new IllegalArgumentException(
                                "external GLTF buffer files are not supported; upload a single .glb file instead");
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid GLTF JSON", ex);
        }
    }

    private static boolean looksLikeModelBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }
        if (bytes[0] == 'g' && bytes[1] == 'l' && bytes[2] == 'T' && bytes[3] == 'F') {
            return true;
        }
        for (int i = 0; i < Math.min(bytes.length, 256); i++) {
            byte b = bytes[i];
            if (!Character.isWhitespace(b)) {
                return b == '{';
            }
        }
        return false;
    }

    private static boolean isAllowedByExtension(String originalFileName, String fileKind) {
        String lower = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);
        if (KIND_MODEL.equals(fileKind)) {
            return lower.endsWith(".glb") || lower.endsWith(".gltf");
        }
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".webp")
                || lower.endsWith(".gif")
                || lower.endsWith(".svg");
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

    private static String normalizeContentType(
            String contentType, String originalFileName, String fileKind, byte[] bytes) {
        if (contentType != null && !contentType.isBlank()) {
            String normalized = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
            if (!"application/octet-stream".equals(normalized) && !"text/plain".equals(normalized)) {
                return normalized;
            }
        }
        String lower = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".glb")) return "model/gltf-binary";
        if (lower.endsWith(".gltf")) return "model/gltf+json";
        if (KIND_MODEL.equals(fileKind) && looksLikeModelBytes(bytes)) {
            if (bytes.length >= 4
                    && bytes[0] == 'g'
                    && bytes[1] == 'l'
                    && bytes[2] == 'T'
                    && bytes[3] == 'F') {
                return "model/gltf-binary";
            }
            return "model/gltf+json";
        }
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return KIND_MODEL.equals(fileKind) ? "application/octet-stream" : "application/octet-stream";
    }

    private static String extensionFor(String contentType, String originalFileName, String fileKind) {
        if (contentType.startsWith("model/gltf") && !"model/gltf-binary".equals(contentType)) {
            return ".gltf";
        }
        return switch (contentType) {
            case "model/gltf-binary" -> ".glb";
            case "model/gltf+json" -> ".gltf";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/svg+xml" -> ".svg";
            default -> {
                String lower = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".glb")) yield ".glb";
                if (lower.endsWith(".gltf")) yield ".gltf";
                if (lower.endsWith(".png")) yield ".png";
                if (lower.endsWith(".webp")) yield ".webp";
                if (lower.endsWith(".gif")) yield ".gif";
                if (lower.endsWith(".svg")) yield ".svg";
                yield KIND_MODEL.equals(fileKind) ? ".glb" : ".jpg";
            }
        };
    }

    public record StoredBimAsset(String url, String storedName, String contentType, long sizeBytes) {}

    public record LoadedBimAsset(Resource resource, String contentType) {}
}
