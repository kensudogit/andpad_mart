package jp.andpad.imart.workflow.model;

/**
 * ステップ種別。
 */
public enum WorkflowStepType {
    /** 起票ステップ（申請者が実行）。 */
    SUBMIT,
    /** 承認ステップ。 */
    APPROVAL,
    /** 通知のみ（自動遷移）。 */
    NOTIFY,
    /** 終了ステップ。 */
    END
}
