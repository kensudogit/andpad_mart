package jp.andpad.api.domain;

public record BudgetLineItem(
        String id,
        String budgetId,
        String categoryCode,
        String categoryName,
        String wbsCode,
        String description,
        double estimateAmount,
        double budgetAmount,
        double committedAmount,
        double actualAmount,
        double varianceAmount,
        double variancePct,
        int sortOrder,
        String createdAt) {}
