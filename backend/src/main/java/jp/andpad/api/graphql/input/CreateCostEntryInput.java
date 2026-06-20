package jp.andpad.api.graphql.input;

import jp.andpad.api.domain.CostEntryType;

public record CreateCostEntryInput(
        String projectId,
        String lineItemId,
        CostEntryType entryType,
        String vendorName,
        String description,
        double amount,
        String entryDate,
        String invoiceNo,
        String recordedBy) {}
