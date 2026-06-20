package jp.andpad.api.domain;

public record CrmContact(
        String id,
        String name,
        String email,
        String phone,
        String company,
        String stage,
        String notes,
        String createdAt) {}
