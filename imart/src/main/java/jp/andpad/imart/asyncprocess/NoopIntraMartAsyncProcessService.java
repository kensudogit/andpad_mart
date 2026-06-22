package jp.andpad.imart.asyncprocess;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;
import jp.andpad.imart.asyncprocess.model.WorkflowAsyncProcessResult;
import jp.andpad.imart.asyncprocess.spi.IntraMartAsyncProcessService;

/** 非同期処理連携無効時の no-op 実装。 */
@Service
@ConditionalOnProperty(name = "app.imart.asyncprocess.enabled", havingValue = "false")
public class NoopIntraMartAsyncProcessService implements IntraMartAsyncProcessService {

    @Override
    public WorkflowAsyncProcessResult createAsyncProcessStatusData(
            String sessionId, List<AsyncProcessStatusData> models) {
        return WorkflowAsyncProcessResult.fail("async process integration is disabled");
    }

    @Override
    public WorkflowAsyncProcessResult updateAsyncProcessStatusData(
            String sessionId, List<AsyncProcessStatusData> models) {
        return WorkflowAsyncProcessResult.fail("async process integration is disabled");
    }

    @Override
    public WorkflowAsyncProcessResult getAsyncProcessStatusDataList(
            String sessionId, AsyncProcessStatusSearchCondition condition) {
        return WorkflowAsyncProcessResult.ok(List.of());
    }

    @Override
    public WorkflowAsyncProcessResult getAsyncProcessStatusDataListCount(
            String sessionId, AsyncProcessStatusSearchCondition condition) {
        return WorkflowAsyncProcessResult.okCount(0L);
    }
}
