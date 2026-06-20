package jp.andpad.api.domain;

public record TeamMember(String id, User user, MemberRole role, String joinedAt, String lastActiveAt) {}
