package jp.andpad.imart.workflow.model;

import java.util.List;

/**
 * 再利用可能なワークフロー定義（フローテンプレート）。
 *
 * @param id          定義 ID（DB 上の主キー、または組み込み定義の論理 ID）
 * @param flowId      論理フロー ID（例: {@code generic-single-approval}）
 * @param name        表示名
 * @param entityType  紐づけ可能な業務エンティティ種別（例: {@code PROJECT_BUDGET}）
 * @param version     定義バージョン
 * @param description 説明
 * @param steps       ステップ定義一覧（stepOrder 昇順）
 */
public record WorkflowDefinition(
        String id,
        String flowId,
        String name,
        String entityType,
        int version,
        String description,
        List<WorkflowStepDefinition> steps) {

    /** 最初の処理対象ステップ（SUBMIT 以外の最初の APPROVAL）。 */
    public WorkflowStepDefinition firstActionStep() {
        return steps.stream()
                .filter(s -> s.stepType() == WorkflowStepType.APPROVAL)
                .findFirst()
                .orElse(steps.stream()
                        .filter(s -> s.stepType() != WorkflowStepType.END)
                        .reduce((a, b) -> b)
                        .orElseThrow());
    }

    /** 指定 stepKey の次ステップ。END の次は empty。 */
    public java.util.Optional<WorkflowStepDefinition> nextStep(String currentStepKey) {
        List<WorkflowStepDefinition> ordered = steps.stream()
                .sorted(java.util.Comparator.comparingInt(WorkflowStepDefinition::stepOrder))
                .toList();
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).stepKey().equals(currentStepKey) && i + 1 < ordered.size()) {
                WorkflowStepDefinition next = ordered.get(i + 1);
                if (next.stepType() == WorkflowStepType.END) {
                    return java.util.Optional.empty();
                }
                return java.util.Optional.of(next);
            }
        }
        return java.util.Optional.empty();
    }

    public WorkflowStepDefinition stepByKey(String stepKey) {
        return steps.stream()
                .filter(s -> s.stepKey().equals(stepKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown step: " + stepKey));
    }
}
