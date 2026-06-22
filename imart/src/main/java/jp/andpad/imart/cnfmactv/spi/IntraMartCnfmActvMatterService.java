package jp.andpad.imart.cnfmactv.spi;

import jp.andpad.imart.cnfmactv.model.CnfmActvMatterSearchCondition;
import jp.andpad.imart.cnfmactv.model.WorkflowCnfmActvMatterResult;

/**
 * intra-mart 未完了案件確認一覧 SPI（{@code CnfmActvMatterList}）。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/CnfmActvMatterList/index.html">CnfmActvMatterList</a>
 */
public interface IntraMartCnfmActvMatterService {

    /** ユーザが確認できる未完了案件一覧（{@code getCnfmList}）。 */
    WorkflowCnfmActvMatterResult getCnfmList(String sessionId, CnfmActvMatterSearchCondition condition);

    /** ユーザが確認できる未完了案件件数（{@code getCnfmListCount}）。 */
    WorkflowCnfmActvMatterResult getCnfmListCount(String sessionId, CnfmActvMatterSearchCondition condition);

    /** ユーザが一括確認できる未完了案件一覧（{@code getLumpCnfmList}）。 */
    WorkflowCnfmActvMatterResult getLumpCnfmList(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition);

    /** ユーザが一括確認できる未完了案件件数（{@code getLumpCnfmListCount}）。 */
    WorkflowCnfmActvMatterResult getLumpCnfmListCount(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition);
}
