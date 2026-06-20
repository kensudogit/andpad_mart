package jp.andpad.api.domain;

public record Session(User user, Organization organization, MemberRole role) {}
