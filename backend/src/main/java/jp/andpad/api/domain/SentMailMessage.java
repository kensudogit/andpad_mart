package jp.andpad.api.domain;

/** 送信済みメールメッセージ。 */
public record SentMailMessage(
        String id,
        String mailId,
        String localeId,
        String recipients,
        String cc,
        String subject,
        String body,
        String entityType,
        String entityId,
        String flowId,
        boolean sent,
        String createdAt) {}
