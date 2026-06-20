package jp.andpad.api.domain;

public record CostEntry(
        String id,
        String projectId,
        String projectName,
        String lineItemId,
        String lineItemName,
        CostEntryType entryType,
        String vendorName,
        String description,
        double amount,
        String entryDate,
        String invoiceNo,
        String recordedBy,
        String createdAt) {}
