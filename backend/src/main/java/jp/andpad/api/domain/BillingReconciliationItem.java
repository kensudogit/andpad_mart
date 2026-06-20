package jp.andpad.api.domain;

public record BillingReconciliationItem(
        String billingRecordId,
        String title,
        double billingAmount,
        double costAmount,
        double varianceAmount,
        String status,
        String billingDate) {}
