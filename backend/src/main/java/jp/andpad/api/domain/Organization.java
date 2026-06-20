package jp.andpad.api.domain;

import java.util.List;

public record Organization(
        String id,
        String name,
        String slug,
        PlanTier planTier,
        SubscriptionStatus subscriptionStatus,
        int seatCount,
        String timezone,
        int memberCount,
        String createdAt,
        List<SaasModule> enabledModules) {}
