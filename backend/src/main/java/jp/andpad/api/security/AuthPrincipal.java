package jp.andpad.api.security;

public record AuthPrincipal(
        String userId,
        String orgId,
        String role,
        String email,
        String name) {}
