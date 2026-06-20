package jp.andpad.api.domain;

import java.util.List;
import java.util.Map;

/** 汎用ワークフロー GraphQL / REST 用ドメイン型。 */
public final class WorkflowTypes {

    private WorkflowTypes() {}

    public record WorkflowDefinitionView(
            String id,
            String flowId,
            String name,
            String entityType,
            int version,
            String description,
            List<WorkflowStepView> steps) {}

    public record WorkflowStepView(
            String stepKey,
            String name,
            int stepOrder,
            String stepType,
            String assigneeType,
            String assigneeValue,
            String imNodeId) {}

    public record WorkflowInstanceView(
            String id,
            String definitionId,
            String flowId,
            String flowName,
            String entityType,
            String entityId,
            String status,
            String currentStepKey,
            String title,
            String payload,
            String submitterUserId,
            String imSystemMatterId,
            List<WorkflowTaskView> tasks,
            List<WorkflowHistoryView> history,
            String createdAt,
            String updatedAt,
            String completedAt) {}

    public record WorkflowTaskView(
            String id,
            String instanceId,
            String stepKey,
            String stepName,
            String assigneeType,
            String assigneeValue,
            String status,
            String actionTaken,
            String comment,
            String actedByUserId,
            String createdAt,
            String completedAt) {}

    public record WorkflowHistoryView(
            String id,
            String stepKey,
            String action,
            String actorUserId,
            String actorName,
            String comment,
            String createdAt) {}
}
