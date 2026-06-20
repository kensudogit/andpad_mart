package jp.andpad.imart.workflow;

import java.util.List;

import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowActor;
import jp.andpad.imart.workflow.model.WorkflowAssigneeType;
import jp.andpad.imart.workflow.model.WorkflowContext;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import jp.andpad.imart.workflow.model.WorkflowStepType;
import jp.andpad.imart.workflow.model.WorkflowTransitionResult;

/**
 * 汎用ワークフロー状態遷移エンジン。
 *
 * <p>定義（{@link WorkflowDefinition}）と現在コンテキスト（{@link WorkflowContext}）に基づき、
 * アクション実行後の状態・次ステップ・タスク生成要否を決定する。永続化は行わない。
 */
public final class GenericWorkflowEngine {

    private GenericWorkflowEngine() {}

    /**
     * アクション実行後の遷移結果を計算する。
     */
    public static WorkflowTransitionResult transition(
            WorkflowDefinition definition, WorkflowContext context, WorkflowAction action) {
        return switch (action) {
            case SUBMIT -> submit(definition, context);
            case APPROVE -> approve(definition, context);
            case REJECT -> terminal(WorkflowInstanceStatus.REJECTED);
            case RETURN -> returnToPrevious(definition, context);
            case CANCEL -> terminal(WorkflowInstanceStatus.CANCELLED);
        };
    }

    /**
     * アクターが指定ステップのタスクを処理できるか判定する。
     */
    public static boolean canAct(
            WorkflowDefinition definition,
            WorkflowContext context,
            WorkflowActor actor,
            String taskStepKey) {
        if (context.instanceStatus() != WorkflowInstanceStatus.RUNNING) {
            return false;
        }
        if (actor.admin()) {
            return true;
        }
        WorkflowStepDefinition step = definition.stepByKey(taskStepKey);
        return matchesAssignee(step, context, actor);
    }

    private static WorkflowTransitionResult submit(WorkflowDefinition definition, WorkflowContext context) {
        if (context.instanceStatus() != WorkflowInstanceStatus.DRAFT) {
            throw new WorkflowException("only DRAFT instances can be submitted");
        }
        WorkflowStepDefinition first = definition.firstActionStep();
        if (first.stepType() == WorkflowStepType.END) {
            return new WorkflowTransitionResult(
                    WorkflowInstanceStatus.APPROVED, null, false, null, true);
        }
        return new WorkflowTransitionResult(
                WorkflowInstanceStatus.RUNNING, first.stepKey(), true, first.stepKey(), false);
    }

    private static WorkflowTransitionResult approve(WorkflowDefinition definition, WorkflowContext context) {
        if (context.instanceStatus() != WorkflowInstanceStatus.RUNNING) {
            throw new WorkflowException("instance is not running");
        }
        if (context.currentStepKey() == null) {
            throw new WorkflowException("current step is not set");
        }
        return definition
                .nextStep(context.currentStepKey())
                .map(next -> {
                    if (next.stepType() == WorkflowStepType.APPROVAL) {
                        return new WorkflowTransitionResult(
                                WorkflowInstanceStatus.RUNNING, next.stepKey(), true, next.stepKey(), false);
                    }
                    return terminal(WorkflowInstanceStatus.APPROVED);
                })
                .orElseGet(() -> terminal(WorkflowInstanceStatus.APPROVED));
    }

    private static WorkflowTransitionResult returnToPrevious(
            WorkflowDefinition definition, WorkflowContext context) {
        if (context.instanceStatus() != WorkflowInstanceStatus.RUNNING) {
            throw new WorkflowException("instance is not running");
        }
        List<WorkflowStepDefinition> ordered = definition.steps().stream()
                .sorted(java.util.Comparator.comparingInt(WorkflowStepDefinition::stepOrder))
                .filter(s -> s.stepType() == WorkflowStepType.APPROVAL
                        || s.stepType() == WorkflowStepType.SUBMIT)
                .toList();
        int idx = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).stepKey().equals(context.currentStepKey())) {
                idx = i;
                break;
            }
        }
        if (idx <= 0) {
            throw new WorkflowException("cannot return from first step");
        }
        WorkflowStepDefinition previous = ordered.get(idx - 1);
        return new WorkflowTransitionResult(
                WorkflowInstanceStatus.RUNNING, previous.stepKey(), true, previous.stepKey(), false);
    }

    private static WorkflowTransitionResult terminal(WorkflowInstanceStatus status) {
        return new WorkflowTransitionResult(status, null, false, null, true);
    }

    private static boolean matchesAssignee(
            WorkflowStepDefinition step, WorkflowContext context, WorkflowActor actor) {
        return switch (step.assigneeType()) {
            case SUBMITTER -> context.submitterUserId().equals(actor.userId());
            case USER -> step.assigneeValue() != null && step.assigneeValue().equals(actor.userId());
            case ROLE -> step.assigneeValue() != null
                    && step.assigneeValue().equalsIgnoreCase(actor.role());
            case ANY -> true;
        };
    }
}
