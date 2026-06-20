package jp.andpad.api.domain;

public record AttendanceRecord(
        String id,
        String userId,
        String userName,
        String clockIn,
        String clockOut,
        String note) {}
