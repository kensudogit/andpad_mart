package jp.andpad.api.domain;

public record LeaveRequest(
        String id,
        String userId,
        String userName,
        String startDate,
        String endDate,
        String reason,
        String status,
        String createdAt) {}
