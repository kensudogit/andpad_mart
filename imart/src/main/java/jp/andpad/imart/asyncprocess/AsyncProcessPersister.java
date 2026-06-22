package jp.andpad.imart.asyncprocess;

import org.springframework.stereotype.Component;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.spi.AsyncProcessRecorder;
import lombok.RequiredArgsConstructor;

/** 非同期処理状況を永続化 SPI へ保存する。 */
@Component
@RequiredArgsConstructor
public class AsyncProcessPersister {

    private final IntraMartAsyncProcessProperties properties;
    private final AsyncProcessRecorder asyncProcessRecorder;

    public void persist(AsyncProcessStatusData data, String workflowInstanceId, String entityType, String entityId) {
        if (properties.isStoreInDatabase()) {
            asyncProcessRecorder.record(data, workflowInstanceId, entityType, entityId);
        }
    }
}
