package jp.andpad.api.auth.dto;

import jp.andpad.api.domain.Session;

public record AuthResponse(String token, Session session) {}
