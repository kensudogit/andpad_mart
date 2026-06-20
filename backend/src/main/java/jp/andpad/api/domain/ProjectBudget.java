package jp.andpad.api.domain;

public record ProjectBudget(
        String id,
        String projectId,
        String projectName,
        String name,
        BudgetType budgetType,
        BudgetStatus status,
        int versionNo,
        double contractAmount,
        double totalEstimate,
        double totalBudget,
        double totalCommitted,
        double totalActual,
        String notes,
        String approvedAt,
        String createdAt) {}
