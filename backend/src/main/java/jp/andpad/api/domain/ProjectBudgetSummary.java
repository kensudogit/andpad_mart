package jp.andpad.api.domain;

public record ProjectBudgetSummary(
        String projectId,
        String projectName,
        ConstructionProjectStatus status,
        double contractAmount,
        double totalBudget,
        double totalActual,
        double billingTotal,
        double variancePct) {}
