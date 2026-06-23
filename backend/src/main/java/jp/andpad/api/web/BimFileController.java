package jp.andpad.api.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jp.andpad.api.bim.BimAssetStorage;
import jp.andpad.api.bim.BimAssetStorage.StoredBimAsset;
import jp.andpad.api.domain.ExtendedTypes.BimModel;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.service.ExtendedService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/saas/bim")
@RequiredArgsConstructor
public class BimFileController {

    private final BimAssetStorage bimAssetStorage;
    private final ExtendedService extendedService;

    @PostMapping(value = "/upload/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadThumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bimModelId", required = false) String bimModelId)
            throws IOException {
        String orgId = requireOrgId();
        StoredBimAsset stored = bimAssetStorage.storeImage(
                orgId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("url", stored.url());
        body.put("fileName", file.getOriginalFilename());
        body.put("contentType", stored.contentType());
        body.put("sizeBytes", stored.sizeBytes());

        if (bimModelId != null && !bimModelId.isBlank()) {
            BimModel updated = extendedService.updateBimModelThumbnail(bimModelId, stored.url());
            body.put("bimModelId", updated.id());
            body.put("bimModel", updated);
        }
        return body;
    }

    @GetMapping("/files/{orgId}/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String orgId, @PathVariable String fileName)
            throws IOException {
        String currentOrg = TenantContext.orgId();
        if (currentOrg == null || !currentOrg.equals(orgId)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = bimAssetStorage.load(orgId, fileName);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .contentType(MediaType.parseMediaType(bimAssetStorage.contentTypeFor(fileName)))
                .body(resource);
    }

    private static String requireOrgId() {
        String orgId = TenantContext.orgId();
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalStateException("organization context is required");
        }
        return orgId;
    }
}
