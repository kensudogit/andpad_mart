package jp.andpad.imart.workflow.model;

/**
 * ワークフロー定義の1ステップ。
 *
 * @param stepKey        ステップ識別子（定義内一意）
 * @param name           表示名
 * @param stepOrder      実行順序（0 始まり）
 * @param stepType       ステップ種別
 * @param assigneeType   担当者解決方式
 * @param assigneeValue  ユーザ ID / ロール ID（{@link WorkflowAssigneeType#ANY} 時は null 可）
 * @param imNodeId       intra-mart IM-Workflow ノード ID（認可ブリッジ用、任意）
 */
public record WorkflowStepDefinition(
        String stepKey,
        String name,
        int stepOrder,
        WorkflowStepType stepType,
        WorkflowAssigneeType assigneeType,
        String assigneeValue,
        String imNodeId) {}
