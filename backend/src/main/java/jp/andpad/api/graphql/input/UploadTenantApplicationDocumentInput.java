package jp.andpad.api.graphql.input;

/** テナント申請書類アップロード入力。 */
public record UploadTenantApplicationDocumentInput(
        String applicationId, String fileName, String contentType, String contentText) {}
