package jp.andpad.api.graphql.input;

/** テナント作成申請の更新入力。 */
public record UpdateTenantApplicationInput(
        String id,
        String name,
        String slug,
        String address,
        String contactName,
        String contactEmail,
        String contactPhone,
        String ownerName,
        String ownerEmail,
        String notes) {}
