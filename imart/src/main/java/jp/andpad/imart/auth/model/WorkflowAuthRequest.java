package jp.andpad.imart.auth.model;

/**
 * {@code WorkflowAuthUtil} 権限判定リクエスト。
 */
public record WorkflowAuthRequest(
        String sessionId,
        String systemMatterId,
        String userDataId,
        String flowId,
        String nodeId,
        String applyBaseDate,
        String authUserCode,
        String admorType) {

    public static WorkflowAuthRequest forApply(String sessionId, String flowId, String applyBaseDate) {
        return new WorkflowAuthRequest(sessionId, null, null, flowId, null, applyBaseDate, null, null);
    }

    public static WorkflowAuthRequest forProcess(String sessionId, String systemMatterId, String nodeId) {
        return new WorkflowAuthRequest(sessionId, systemMatterId, null, null, nodeId, null, null, null);
    }

    public static WorkflowAuthRequest forConfirm(String sessionId, String systemMatterId, String nodeId) {
        return new WorkflowAuthRequest(sessionId, systemMatterId, null, null, nodeId, null, null, null);
    }

    public static WorkflowAuthRequest forReference(String sessionId, String systemMatterId, String admorType) {
        return new WorkflowAuthRequest(sessionId, systemMatterId, null, null, null, null, null, admorType);
    }
}
