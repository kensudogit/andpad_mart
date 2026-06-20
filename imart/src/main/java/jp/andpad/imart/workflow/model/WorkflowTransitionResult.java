package jp.andpad.imart.workflow.model;

/**
 * ワークフロー遷移結果。
 *
 * @param nextStatus     遷移後インスタンス状態
 * @param nextStepKey    遷移後ステップ key（終了時は null）
 * @param createTask     新規タスクを生成するか
 * @param taskStepKey    生成するタスクのステップ key
 * @param terminal       フロー終了か
 */
public record WorkflowTransitionResult(
        WorkflowInstanceStatus nextStatus,
        String nextStepKey,
        boolean createTask,
        String taskStepKey,
        boolean terminal) {}
