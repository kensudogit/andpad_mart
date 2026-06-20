package jp.andpad.api.domain;

public record DxInitiative(
        String id,
        String title,
        String description,
        String status,
        int progressPct,
        String ownerName,
        String dueDate,
        int taskCount,
        int tasksDone,
        String createdAt) {}
