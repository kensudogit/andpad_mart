package jp.andpad.imart.workflow.model;

/**
 * タスク処理権限チェック用アクター情報。
 *
 * @param userId  ユーザ ID
 * @param role    組織ロール（例: {@code OWNER}、{@code admin}）
 * @param admin   管理者フラグ
 */
public record WorkflowActor(String userId, String role, boolean admin) {}
