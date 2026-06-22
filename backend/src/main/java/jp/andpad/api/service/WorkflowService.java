package jp.andpad.api.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.WorkflowTypes.WorkflowDefinitionView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowInstanceView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowTaskView;
import jp.andpad.api.repository.WorkflowRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.security.UnauthorizedException;
import jp.andpad.imart.workflow.GenericWorkflowEngine;
import jp.andpad.imart.workflow.WorkflowException;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowActor;
import jp.andpad.imart.workflow.model.WorkflowContext;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import jp.andpad.imart.workflow.model.WorkflowTransitionResult;
import jp.andpad.imart.workflow.spi.WorkflowAuthGuard;
import jp.andpad.imart.mail.WorkflowMailNotifier;
import jp.andpad.imart.asyncprocess.WorkflowAsyncProcessRecorder;
import jp.andpad.imart.monitoring.WorkflowMonitoringRecorder;
import jp.andpad.imart.stamp.WorkflowStampRecorder;
import org.springframework.context.annotation.Lazy;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowAuthGuard workflowAuthGuard;
    private final WorkflowMailNotifier workflowMailNotifier;
    private final WorkflowMonitoringRecorder workflowMonitoringRecorder;
    private final WorkflowStampRecorder workflowStampRecorder;
    private final WorkflowAsyncProcessRecorder workflowAsyncProcessRecorder;
    private final TenantApplicationService tenantApplicationService;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            WorkflowAuthGuard workflowAuthGuard,
            WorkflowMailNotifier workflowMailNotifier,
            WorkflowMonitoringRecorder workflowMonitoringRecorder,
            WorkflowStampRecorder workflowStampRecorder,
            WorkflowAsyncProcessRecorder workflowAsyncProcessRecorder,
            @Lazy TenantApplicationService tenantApplicationService) {
        this.workflowRepository = workflowRepository;
        this.workflowAuthGuard = workflowAuthGuard;
        this.workflowMailNotifier = workflowMailNotifier;
        this.workflowMonitoringRecorder = workflowMonitoringRecorder;
        this.workflowStampRecorder = workflowStampRecorder;
        this.workflowAsyncProcessRecorder = workflowAsyncProcessRecorder;
        this.tenantApplicationService = tenantApplicationService;
    }

    public List<WorkflowDefinitionView> listDefinitions() {
        return workflowRepository.listDefinitions(TenantContext.orgId());
    }

    public List<WorkflowInstanceView> listInstances(String entityType, String status) {
        return workflowRepository.listInstances(TenantContext.orgId(), entityType, status);
    }

    public WorkflowInstanceView getInstance(String instanceId) {
        return workflowRepository
                .findInstance(TenantContext.orgId(), instanceId)
                .orElseThrow(() -> new WorkflowException("workflow instance not found: " + instanceId));
    }

    public List<WorkflowTaskView> myPendingTasks() {
        var principal = TenantContext.requirePrincipal();
        WorkflowActor actor = toActor(principal.userId(), principal.role());
        return workflowRepository.listMyPendingTasks(
                TenantContext.orgId(), principal.userId(), principal.role(), actor.admin());
    }

    public WorkflowInstanceView startWorkflow(
            String flowId,
            String entityType,
            String entityId,
            String title,
            Map<String, Object> payload,
            String imSessionId,
            String imSystemMatterId) {
        var principal = TenantContext.requirePrincipal();
        WorkflowDefinitionView defView = workflowRepository
                .findDefinitionByFlowId(TenantContext.orgId(), flowId)
                .orElseThrow(() -> new WorkflowException("workflow definition not found: " + flowId));

        WorkflowInstanceView draft = workflowRepository.createInstance(
                TenantContext.orgId(),
                defView.id(),
                entityType != null ? entityType : defView.entityType(),
                entityId,
                title,
                payload,
                principal.userId(),
                imSystemMatterId);

        return submitInstance(draft.id(), imSessionId);
    }

    public WorkflowInstanceView submitWorkflow(String instanceId, String imSessionId) {
        WorkflowInstanceView instance = getInstance(instanceId);
        if (!instance.submitterUserId().equals(TenantContext.requirePrincipal().userId())
                && !toActor(TenantContext.userId(), TenantContext.requirePrincipal().role()).admin()) {
            throw new UnauthorizedException("only submitter or admin can submit");
        }
        return submitInstance(instanceId, imSessionId);
    }

    public WorkflowInstanceView completeTask(
            String taskId, WorkflowAction action, String comment, String imSessionId) {
        var principal = TenantContext.requirePrincipal();
        WorkflowTaskView task = workflowRepository
                .findTask(TenantContext.orgId(), taskId)
                .orElseThrow(() -> new WorkflowException("task not found: " + taskId));
        if (!"PENDING".equals(task.status())) {
            throw new WorkflowException("task is not pending");
        }

        WorkflowInstanceView instance = getInstance(task.instanceId());
        WorkflowDefinition definition = workflowRepository.loadEngineDefinition(instance.definitionId());
        WorkflowContext context = toContext(instance);
        WorkflowActor actor = toActor(principal.userId(), principal.role());

        if (!GenericWorkflowEngine.canAct(definition, context, actor, task.stepKey())) {
            throw new UnauthorizedException("not authorized for this workflow task");
        }
        if (!workflowAuthGuard.authorize(
                imSessionId,
                definition,
                instance.imSystemMatterId(),
                task.stepKey(),
                action)) {
            throw new UnauthorizedException("IM workflow authorization denied");
        }

        WorkflowTransitionResult result = GenericWorkflowEngine.transition(definition, context, action);
        workflowRepository.completeTask(TenantContext.orgId(), taskId, action, principal.userId(), comment);
        workflowRepository.addHistory(
                TenantContext.orgId(),
                instance.id(),
                task.stepKey(),
                action.name(),
                principal.userId(),
                principal.name(),
                comment,
                Map.of());

        if (result.createTask()) {
            WorkflowStepDefinition step = definition.stepByKey(result.taskStepKey());
            workflowRepository.createTask(
                    TenantContext.orgId(),
                    instance.id(),
                    step.stepKey(),
                    step.assigneeType(),
                    resolveAssigneeValue(step, instance.submitterUserId()));
        }

        WorkflowInstanceView updated = workflowRepository.updateInstanceState(
                TenantContext.orgId(),
                instance.id(),
                result.nextStatus(),
                result.nextStepKey(),
                result.terminal());

        workflowMailNotifier.notifyTransition(
                definition,
                instance.id(),
                instance.entityId(),
                instance.title(),
                principal.name(),
                definition.stepByKey(task.stepKey()),
                action,
                result.nextStatus(),
                result.nextStepKey(),
                comment,
                imSessionId);

        workflowMonitoringRecorder.recordTransition(
                definition,
                instance.id(),
                action,
                result.nextStatus(),
                instance.createdAt(),
                updated.completedAt(),
                imSessionId);

        workflowStampRecorder.recordCompletion(
                definition,
                instance.id(),
                instance.entityType(),
                instance.entityId(),
                action,
                result.nextStatus(),
                task.stepKey(),
                principal.name(),
                instance.imSystemMatterId(),
                imSessionId);

        workflowAsyncProcessRecorder.recordCompletion(
                definition,
                instance.id(),
                instance.entityType(),
                instance.entityId(),
                instance.title(),
                action,
                result.nextStatus(),
                task.stepKey(),
                principal.userId(),
                instance.imSystemMatterId(),
                imSessionId);

        if ("TENANT_APPLICATION".equals(instance.entityType())) {
            tenantApplicationService.handleWorkflowCompletion(instance.entityId(), result.nextStatus());
        }

        return updated;
    }

    private WorkflowInstanceView submitInstance(String instanceId, String imSessionId) {
        WorkflowInstanceView instance = getInstance(instanceId);
        WorkflowDefinition definition = workflowRepository.loadEngineDefinition(instance.definitionId());
        WorkflowContext context = toContext(instance);

        if (!workflowAuthGuard.authorize(
                imSessionId, definition, instance.imSystemMatterId(), "submit", WorkflowAction.SUBMIT)) {
            throw new UnauthorizedException("IM workflow authorization denied");
        }

        WorkflowTransitionResult result =
                GenericWorkflowEngine.transition(definition, context, WorkflowAction.SUBMIT);
        if (result.createTask()) {
            WorkflowStepDefinition step = definition.stepByKey(result.taskStepKey());
            workflowRepository.createTask(
                    TenantContext.orgId(),
                    instance.id(),
                    step.stepKey(),
                    step.assigneeType(),
                    resolveAssigneeValue(step, instance.submitterUserId()));
        }
        workflowRepository.addHistory(
                TenantContext.orgId(),
                instance.id(),
                "submit",
                WorkflowAction.SUBMIT.name(),
                TenantContext.requirePrincipal().userId(),
                TenantContext.requirePrincipal().name(),
                "submitted",
                Map.of());
        WorkflowInstanceView updated = workflowRepository.updateInstanceState(
                TenantContext.orgId(),
                instance.id(),
                result.nextStatus(),
                result.nextStepKey(),
                result.terminal());

        workflowMailNotifier.notifyTransition(
                definition,
                instance.id(),
                instance.entityId(),
                instance.title(),
                TenantContext.requirePrincipal().name(),
                definition.stepByKey("submit"),
                WorkflowAction.SUBMIT,
                result.nextStatus(),
                result.nextStepKey(),
                "submitted",
                imSessionId);

        workflowMonitoringRecorder.ensureFlowMonitoring(definition, imSessionId);

        workflowAsyncProcessRecorder.recordSubmit(
                definition,
                updated.id(),
                instance.entityType(),
                instance.entityId(),
                instance.title(),
                TenantContext.requirePrincipal().userId(),
                instance.imSystemMatterId(),
                imSessionId);

        return updated;
    }

    private static WorkflowContext toContext(WorkflowInstanceView instance) {
        return new WorkflowContext(
                WorkflowInstanceStatus.valueOf(instance.status()),
                instance.currentStepKey(),
                instance.submitterUserId());
    }

    private static WorkflowActor toActor(String userId, String role) {
        boolean admin = role != null
                && (role.equalsIgnoreCase("OWNER") || role.equalsIgnoreCase("admin"));
        String normalizedRole = role != null && role.equalsIgnoreCase("OWNER") ? "admin" : role;
        return new WorkflowActor(userId, normalizedRole, admin);
    }

    private static String resolveAssigneeValue(WorkflowStepDefinition step, String submitterUserId) {
        return switch (step.assigneeType()) {
            case SUBMITTER -> submitterUserId;
            case USER, ROLE -> step.assigneeValue();
            case ANY -> null;
        };
    }
}
