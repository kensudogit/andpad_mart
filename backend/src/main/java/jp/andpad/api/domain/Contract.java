package jp.andpad.api.domain;

public record Contract(
        String id,
        String templateId,
        String title,
        String partyName,
        String partyEmail,
        String body,
        String status,
        String createdAt,
        String signedAt) {}
