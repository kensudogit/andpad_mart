package jp.andpad.api.domain;

/** テナント申請に添付された書類（メタデータ）。 */
public record TenantApplicationDocument(
        String id, String applicationId, String fileName, String contentType, String uploadedAt) {}
