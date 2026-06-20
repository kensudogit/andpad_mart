package jp.andpad.api.domain;

public record ProjectModuleRecord(
        String id,
        String projectId,
        String projectName,
        SaasModuleCode moduleCode,
        String title,
        String status,
        String detail,
        Double amount,
        String personName,
        String recordDate,
        String createdAt) {}
