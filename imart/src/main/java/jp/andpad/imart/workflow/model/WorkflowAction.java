package jp.andpad.imart.workflow.model;

/**
 * タスクに対する操作アクション。
 */
public enum WorkflowAction {
    /** 申請・起票（DRAFT → RUNNING）。 */
    SUBMIT,
    /** 承認して次ステップへ。 */
    APPROVE,
    /** 却下してフロー終了。 */
    REJECT,
    /** 前ステップへ差戻し。 */
    RETURN,
    /** フロー取消。 */
    CANCEL
}
