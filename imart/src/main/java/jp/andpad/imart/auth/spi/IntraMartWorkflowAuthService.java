package jp.andpad.imart.auth.spi;

import jp.andpad.imart.auth.model.WorkflowAuthRequest;

/**
 * IM-Workflow ワークフロー権限 SPI。
 *
 * <p>intra-mart SSJS API の {@code WorkflowAuthUtil} に相当する操作を抽象化する。
 * 申請・処理・確認・参照各画面の表示権限および操作権限を判定する。
 *
 * <p>実装:
 * <ul>
 *   <li>{@link jp.andpad.imart.auth.stub.DevIntraMartWorkflowAuthService} — ローカルスタブ</li>
 *   <li>{@link jp.andpad.imart.auth.http.HttpIntraMartWorkflowAuthService} — HTTP ブリッジ</li>
 * </ul>
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/WorkflowAuthUtil/index.html">WorkflowAuthUtil</a>
 */
public interface IntraMartWorkflowAuthService {

    /**
     * 申請画面の表示権限を判定する。
     *
     * <p>IM API 相当: {@code WorkflowAuthUtil.isAuthApply(flowId, applyBaseDate, authUserCode)}
     *
     * @param request 権限判定リクエスト
     * @return 権限があれば {@code true}
     */
    boolean isAuthApply(WorkflowAuthRequest request);

    /**
     * 処理画面の表示権限を判定する。
     *
     * <p>IM API 相当: {@code WorkflowAuthUtil.isAuthProcess(systemMatterId, nodeId)}
     *
     * @param request 権限判定リクエスト
     * @return 権限があれば {@code true}
     */
    boolean isAuthProcess(WorkflowAuthRequest request);

    /**
     * 確認画面の表示権限を判定する。
     *
     * <p>IM API 相当: {@code WorkflowAuthUtil.isAuthConfirm(systemMatterId, nodeId)}
     *
     * @param request 権限判定リクエスト
     * @return 権限があれば {@code true}
     */
    boolean isAuthConfirm(WorkflowAuthRequest request);

    /**
     * 参照詳細画面の表示権限を判定する。
     *
     * <p>IM API 相当: {@code WorkflowAuthUtil.isAuthRefDetail(systemMatterId, admorType)}
     *
     * @param request 権限判定リクエスト
     * @return 権限があれば {@code true}
     */
    boolean isAuthRefDetail(WorkflowAuthRequest request);

    /**
     * 申請操作権限を判定する。
     *
     * <p>IM API 相当: {@code WorkflowAuthUtil.canApply(applicationKey)}
     *
     * @param request 権限判定リクエスト
     * @return 申請可能なら {@code true}
     */
    boolean canApply(WorkflowAuthRequest request);

    /**
     * 処理操作権限を判定する。
     *
     * <p>IM API 相当: {@code WorkflowAuthUtil.canProcess(matterProcessKey, asyncProcessingFlag)}
     *
     * @param request 権限判定リクエスト
     * @return 処理可能なら {@code true}
     */
    boolean canProcess(WorkflowAuthRequest request);
}
