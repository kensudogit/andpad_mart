package jp.andpad.api.graphql.input;

import jp.andpad.api.domain.ConstructionProjectStatus;

public record CreateConstructionProjectInput(
        String name,
        String siteAddress,
        ConstructionProjectStatus status,
        String managerName,
        String startDate,
        String endDate) {}
