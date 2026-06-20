package jp.andpad.api.domain;

public record ConstructionProject(
        String id,
        String name,
        String siteAddress,
        ConstructionProjectStatus status,
        String managerName,
        String startDate,
        String endDate,
        int recordCount,
        String createdAt) {}
