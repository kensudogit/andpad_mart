package jp.andpad.imart.workflow.model;

/**
 * ワークフローインスタンスのライフサイクル状態。
 */
public enum WorkflowInstanceStatus {
    /** 起票前・一時保存。 */
    DRAFT,
    /** 承認フロー実行中。 */
    RUNNING,
    /** 最終承認完了。 */
    APPROVED,
    /** 却下。 */
    REJECTED,
    /** 申請者または管理者による取消。 */
    CANCELLED
}
