package jp.andpad.imart.auth.model;

/**
 * IM-Workflow {@code WorkflowAuthUtil} 権限判定リクエスト DTO。
 *
 * <p>各判定メソッド（{@code isAuthApply}、{@code isAuthProcess} 等）に必要な
 * パラメータをまとめて保持する。用途別ファクトリメソッドで必要なフィールドのみ設定する。
 *
 * @param sessionId      IM セッション ID（必須）
 * @param systemMatterId システム案件 ID
 * @param userDataId     ユーザデータ ID
 * @param flowId         フロー ID
 * @param nodeId         ノード ID
 * @param applyBaseDate  申請基準日（{@code yyyy/MM/dd}）
 * @param authUserCode   申請権限者コード
 * @param admorType      管理者種別（参照詳細画面用）
 */
public record WorkflowAuthRequest(
        String sessionId,
        String systemMatterId,
        String userDataId,
        String flowId,
        String nodeId,
        String applyBaseDate,
        String authUserCode,
        String admorType) {

    /**
     * 申請画面表示権限判定（{@code isAuthApply}）用リクエストを生成する。
     *
     * @param sessionId     IM セッション ID
     * @param flowId        フロー ID
     * @param applyBaseDate 申請基準日
     * @return 申請権限判定リクエスト
     */
    public static WorkflowAuthRequest forApply(String sessionId, String flowId, String applyBaseDate) {
        return new WorkflowAuthRequest(sessionId, null, null, flowId, null, applyBaseDate, null, null);
    }

    /**
     * 処理画面表示権限判定（{@code isAuthProcess}）用リクエストを生成する。
     *
     * @param sessionId      IM セッション ID
     * @param systemMatterId システム案件 ID
     * @param nodeId         ノード ID
     * @return 処理権限判定リクエスト
     */
    public static WorkflowAuthRequest forProcess(String sessionId, String systemMatterId, String nodeId) {
        return new WorkflowAuthRequest(sessionId, systemMatterId, null, null, nodeId, null, null, null);
    }

    /**
     * 確認画面表示権限判定（{@code isAuthConfirm}）用リクエストを生成する。
     *
     * @param sessionId      IM セッション ID
     * @param systemMatterId システム案件 ID
     * @param nodeId         ノード ID
     * @return 確認権限判定リクエスト
     */
    public static WorkflowAuthRequest forConfirm(String sessionId, String systemMatterId, String nodeId) {
        return new WorkflowAuthRequest(sessionId, systemMatterId, null, null, nodeId, null, null, null);
    }

    /**
     * 参照詳細画面表示権限判定（{@code isAuthRefDetail}）用リクエストを生成する。
     *
     * @param sessionId      IM セッション ID
     * @param systemMatterId システム案件 ID
     * @param admorType      管理者種別
     * @return 参照権限判定リクエスト
     */
    public static WorkflowAuthRequest forReference(String sessionId, String systemMatterId, String admorType) {
        return new WorkflowAuthRequest(sessionId, systemMatterId, null, null, null, null, null, admorType);
    }
}
