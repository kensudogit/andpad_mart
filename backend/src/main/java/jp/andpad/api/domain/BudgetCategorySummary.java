package jp.andpad.api.domain;

public record BudgetCategorySummary(
        String categoryCode,
        String categoryName,
        double budgetAmount,
        double actualAmount,
        double varianceAmount) {}
