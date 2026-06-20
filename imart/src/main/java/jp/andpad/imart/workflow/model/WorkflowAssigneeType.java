package jp.andpad.imart.workflow.model;

/**
 * タスク担当者の解決方式。
 */
public enum WorkflowAssigneeType {
    /** 申請者本人。 */
    SUBMITTER,
    /** 特定ユーザ ID。 */
    USER,
    /** ロール（組織内 role / IM ロール ID）。 */
    ROLE,
    /** 任意の認証済みユーザ（管理者代理等）。 */
    ANY
}
