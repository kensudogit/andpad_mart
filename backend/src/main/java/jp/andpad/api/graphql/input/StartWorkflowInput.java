package jp.andpad.api.graphql.input;

import java.util.Map;

public record StartWorkflowInput(
        String flowId,
        String entityType,
        String entityId,
        String title,
        Map<String, Object> payload,
        String imSystemMatterId) {}
