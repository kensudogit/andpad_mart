package jp.andpad.api.domain;

public record CrmInteraction(String id, String contactId, String kind, String summary, String occurredAt) {}
