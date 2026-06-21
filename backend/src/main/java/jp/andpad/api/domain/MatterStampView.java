package jp.andpad.api.domain;

/** GraphQL 用の印影データビュー。 */
public record MatterStampView(
        String id,
        String systemMatterId,
        String no,
        String nodeId,
        String processDate,
        String processId,
        String stampStr1,
        String stampStr1Type,
        String stampStr2,
        String stampStr2Type,
        String stampStr3,
        String stampStr3Type,
        String stampType,
        String cancelFlag,
        String flowId,
        String entityType,
        String entityId,
        String workflowInstanceId,
        String createdAt) {}
