package jp.andpad.imart.stamp.spi;

import jp.andpad.imart.stamp.model.StampListSearchCondition;
import jp.andpad.imart.stamp.model.WorkflowStampResult;

/**
 * intra-mart 完了案件印影 SPI（{@code CplMatterStampList}）。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/CplMatterStampList/index.html">CplMatterStampList</a>
 */
public interface IntraMartStampService {

    /** 完了案件の印影データを取得する。 */
    WorkflowStampResult getStampList(
            String sessionId, String systemMatterId, StampListSearchCondition condition);

    /** 完了案件の印影データ件数を取得する。 */
    WorkflowStampResult getStampListCount(
            String sessionId, String systemMatterId, StampListSearchCondition condition);

    /** 完了案件の印影フレームデータを取得する。 */
    WorkflowStampResult getStampResultFrameList(String sessionId, String systemMatterId, boolean sortType);
}
