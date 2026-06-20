package jp.andpad.imart.workflow.spi;

import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;

/**
 * ワークフロー操作前の外部認可チェック SPI（intra-mart IM-Workflow 連携等）。
 */
public interface WorkflowAuthGuard {

    /**
     * 操作が許可されるか判定する。
     *
     * @param sessionId      IM セッション ID（未使用時は null）
     * @param definition     ワークフロー定義
     * @param systemMatterId IM システム案件 ID（未連携時は null）
     * @param stepKey        現在ステップ key
     * @param action         実行アクション
     * @return 許可なら {@code true}（ガード無効時も {@code true}）
     */
    boolean authorize(
            String sessionId,
            WorkflowDefinition definition,
            String systemMatterId,
            String stepKey,
            WorkflowAction action);
}
