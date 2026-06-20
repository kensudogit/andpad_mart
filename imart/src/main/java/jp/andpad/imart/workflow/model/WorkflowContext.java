package jp.andpad.imart.workflow.model;

/**
 * ワークフロー実行コンテキスト（エンジン入出力用）。
 *
 * @param instanceStatus  現在のインスタンス状態
 * @param currentStepKey  現在ステップ key（RUNNING 時）
 * @param submitterUserId 申請者ユーザ ID
 */
public record WorkflowContext(
        WorkflowInstanceStatus instanceStatus,
        String currentStepKey,
        String submitterUserId) {}
