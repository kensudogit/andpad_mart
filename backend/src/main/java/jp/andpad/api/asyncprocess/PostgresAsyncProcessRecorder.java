package jp.andpad.api.asyncprocess;

import org.springframework.stereotype.Component;

import jp.andpad.api.repository.AsyncProcessRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.spi.AsyncProcessRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 非同期処理状況を PostgreSQL に保存する。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresAsyncProcessRecorder implements AsyncProcessRecorder {

    private final AsyncProcessRepository asyncProcessRepository;

    @Override
    public void record(AsyncProcessStatusData data, String workflowInstanceId, String entityType, String entityId) {
        try {
            String orgId = TenantContext.orgId();
            if (orgId == null) {
                log.warn("async process status not stored: organization context missing");
                return;
            }
            asyncProcessRepository.upsert(orgId, data, workflowInstanceId, entityType, entityId);
        } catch (Exception ex) {
            log.warn("failed to store async process status: {}", ex.getMessage());
        }
    }
}
