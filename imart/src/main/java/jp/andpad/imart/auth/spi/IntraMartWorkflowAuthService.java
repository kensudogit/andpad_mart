package jp.andpad.imart.auth.spi;

import jp.andpad.imart.auth.model.WorkflowAuthRequest;

/**
 * IM-Workflow {@code WorkflowAuthUtil} 相当の SPI。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/WorkflowAuthUtil/index.html">WorkflowAuthUtil</a>
 */
public interface IntraMartWorkflowAuthService {

    /** 申請画面の表示権限（{@code isAuthApply}）。 */
    boolean isAuthApply(WorkflowAuthRequest request);

    /** 処理画面の表示権限（{@code isAuthProcess}）。 */
    boolean isAuthProcess(WorkflowAuthRequest request);

    /** 確認画面の表示権限（{@code isAuthConfirm}）。 */
    boolean isAuthConfirm(WorkflowAuthRequest request);

    /** 参照詳細画面の表示権限（{@code isAuthRefDetail}）。 */
    boolean isAuthRefDetail(WorkflowAuthRequest request);

    /** 申請権限（{@code canApply}）。 */
    boolean canApply(WorkflowAuthRequest request);

    /** 処理権限（{@code canProcess}）。 */
    boolean canProcess(WorkflowAuthRequest request);
}
