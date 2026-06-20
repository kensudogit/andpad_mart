package jp.andpad.api.graphql.input;

import jp.andpad.api.domain.BudgetStatus;
import jp.andpad.api.domain.BudgetType;

public record CreateProjectBudgetInput(
        String projectId,
        String name,
        BudgetType budgetType,
        BudgetStatus status,
        Integer versionNo,
        Double contractAmount,
        String notes) {}
