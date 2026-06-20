package jp.andpad.api.domain;

public record UsageSummary(
        int members,
        int membersLimit,
        int videos,
        int videosLimit,
        int apiCallsThisMonth,
        int apiCallsLimit,
        int consultTokensMonth) {}
