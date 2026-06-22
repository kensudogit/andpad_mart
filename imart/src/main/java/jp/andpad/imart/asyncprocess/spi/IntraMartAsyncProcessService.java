package jp.andpad.imart.asyncprocess.spi;

import java.util.List;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;
import jp.andpad.imart.asyncprocess.model.WorkflowAsyncProcessResult;

/**
 * intra-mart 非同期処理ワークフロー SPI。
 *
 * <p>IM SSJS API 相当:
 * <ul>
 *   <li>{@code AsyncProcessWorkflow.createAsyncProcessStatusData}</li>
 *   <li>{@code AsyncProcessWorkflow.updateAsyncProcessStatusData}</li>
 *   <li>{@code AsyncProcessWorkflow.getAsyncProcessStatusDataList}</li>
 * </ul>
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/AsyncProcessWorkflow/index.html">AsyncProcessWorkflow</a>
 */
public interface IntraMartAsyncProcessService {

    WorkflowAsyncProcessResult createAsyncProcessStatusData(String sessionId, List<AsyncProcessStatusData> models);

    WorkflowAsyncProcessResult updateAsyncProcessStatusData(String sessionId, List<AsyncProcessStatusData> models);

    WorkflowAsyncProcessResult getAsyncProcessStatusDataList(
            String sessionId, AsyncProcessStatusSearchCondition condition);

    WorkflowAsyncProcessResult getAsyncProcessStatusDataListCount(
            String sessionId, AsyncProcessStatusSearchCondition condition);
}
