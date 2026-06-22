package jp.andpad.api.domain;

public record CnfmActvMatterView(
        String id,
        String listType,
        String systemMatterId,
        String flowId,
        String flowName,
        String matterName,
        String matterNumber,
        String nodeId,
        String applyAuthUserCode,
        String applyAuthUserName,
        String applyDate,
        String arrivedDate,
        String confirmCplFlag,
        String priorityLevel,
        String updatedAt) {}
