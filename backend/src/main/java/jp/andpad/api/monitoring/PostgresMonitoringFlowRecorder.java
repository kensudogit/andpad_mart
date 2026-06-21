package jp.andpad.api.monitoring;

import org.springframework.stereotype.Component;

import jp.andpad.api.repository.MonitoringFlowRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.spi.MonitoringFlowRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** フロー別モニタリング情報を PostgreSQL に保存する。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresMonitoringFlowRecorder implements MonitoringFlowRecorder {

    private final MonitoringFlowRepository monitoringFlowRepository;

    @Override
    public void record(MonitoringFlowData data) {
        try {
            String orgId = TenantContext.orgId();
            if (orgId == null) {
                log.warn("monitoring flow data not stored: organization context missing");
                return;
            }
            monitoringFlowRepository.upsert(orgId, data);
        } catch (Exception ex) {
            log.warn("failed to store monitoring flow data: {}", ex.getMessage());
        }
    }
}
