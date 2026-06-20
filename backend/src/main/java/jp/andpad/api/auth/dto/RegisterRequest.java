package jp.andpad.api.auth.dto;

public record RegisterRequest(String clinicName, String slug, String ownerName, String email, String password) {}
