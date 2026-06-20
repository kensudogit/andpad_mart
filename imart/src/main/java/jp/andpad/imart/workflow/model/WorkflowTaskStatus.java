package jp.andpad.imart.workflow.model;

/**
 * ワークフロータスクの状態。
 */
public enum WorkflowTaskStatus {
    /** 処理待ち。 */
    PENDING,
    /** 処理完了。 */
    COMPLETED,
    /** スキップ（差戻し等で無効化）。 */
    SKIPPED
}
