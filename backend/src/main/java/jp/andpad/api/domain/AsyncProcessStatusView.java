package jp.andpad.api.domain;

/** GraphQL 向け非同期処理状況ビュー。 */
public record AsyncProcessStatusView(
        String id,
        String acceptId,
        String asyncProcStatus,
        String authUserCode,
        String executeUserCode,
        String flowId,
        String matterName,
        String matterNumber,
        String message,
        String nodeId,
        String procComment,
        String procDate,
        String procType,
        String queueId,
        String subMessage,
        String systemMatterId,
        String entityType,
        String entityId,
        String workflowInstanceId,
        String updatedAt) {}
