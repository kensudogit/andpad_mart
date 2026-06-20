package jp.andpad.api.domain;

import java.util.List;

public record BudgetDashboard(
        String projectId,
        String projectName,
        double contractAmount,
        double totalEstimate,
        double totalBudget,
        double totalCommitted,
        double totalActual,
        double totalForecast,
        double varianceAmount,
        double variancePct,
        double completionPct,
        double estimateBudgetTotal,
        double grossMarginPct,
        double inquiryProfitTotal,
        double billingTotal,
        double billingBalance,
        List<MonthlyCostMetric> monthlyCosts,
        List<BillingReconciliationItem> reconciliation,
        List<BudgetLineItem> lineItems,
        List<CostEntry> recentCosts,
        List<BudgetCategorySummary> categorySummary,
        String generatedAt) {}
