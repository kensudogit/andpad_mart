package jp.andpad.api.graphql.input;

public record CreateBudgetLineItemInput(
        String budgetId,
        String categoryCode,
        String categoryName,
        String wbsCode,
        String description,
        Double estimateAmount,
        Double budgetAmount,
        Double committedAmount,
        Integer sortOrder) {}
