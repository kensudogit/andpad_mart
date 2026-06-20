package jp.andpad.api.graphql.input;

public record CreateDxInitiativeInput(
        String title,
        String description,
        String status,
        Integer progressPct,
        String ownerName,
        String dueDate) {}
