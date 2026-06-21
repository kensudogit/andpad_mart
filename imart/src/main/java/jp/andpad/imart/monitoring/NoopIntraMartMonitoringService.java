package jp.andpad.imart.monitoring;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;
import jp.andpad.imart.monitoring.spi.IntraMartMonitoringService;

/** モニタリング無効時の no-op 実装。 */
@Service
@ConditionalOnProperty(name = "app.imart.monitoring.enabled", havingValue = "false")
public class NoopIntraMartMonitoringService implements IntraMartMonitoringService {

    @Override
    public WorkflowMonitoringResult createMonitoringFlowData(String sessionId, List<MonitoringFlowData> models) {
        return WorkflowMonitoringResult.fail("monitoring is disabled");
    }

    @Override
    public WorkflowMonitoringResult updateMonitoringFlowData(String sessionId, List<MonitoringFlowData> models) {
        return WorkflowMonitoringResult.fail("monitoring is disabled");
    }

    @Override
    public WorkflowMonitoringResult getMonitoringFlowDataList(
            String sessionId, MonitoringFlowDataSearchCondition condition) {
        return WorkflowMonitoringResult.ok(List.of());
    }
}
