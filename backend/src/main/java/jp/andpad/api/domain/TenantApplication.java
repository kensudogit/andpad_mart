package jp.andpad.api.domain;

import java.util.List;

/** 新規テナント作成申請。 */
public record TenantApplication(
        String id,
        String applicantOrgId,
        String applicantUserId,
        String name,
        String slug,
        String address,
        String contactName,
        String contactEmail,
        String contactPhone,
        String ownerName,
        String ownerEmail,
        String notes,
        TenantApplicationStatus status,
        String workflowInstanceId,
        String createdOrgId,
        String submittedAt,
        String approvedAt,
        String createdAt,
        String updatedAt,
        List<TenantApplicationDocument> documents) {}
