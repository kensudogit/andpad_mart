package jp.andpad.imart.asyncprocess;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.spi.AsyncProcessRecorder;

/** DB 永続化未設定時の no-op 実装。 */
@Component
@ConditionalOnMissingBean(AsyncProcessRecorder.class)
public class NoopAsyncProcessRecorder implements AsyncProcessRecorder {

    @Override
    public void record(AsyncProcessStatusData data, String workflowInstanceId, String entityType, String entityId) {
        // no-op
    }
}
