package jp.andpad.api.domain;

/** テナント作成申請のステータス。 */
public enum TenantApplicationStatus {
    DRAFT,
    SUBMITTED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    ACTIVE
}
