package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.MonitoringFlowDataView;
import jp.andpad.api.repository.MonitoringFlowRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.spi.IntraMartMonitoringService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitoringFlowService {

    private final MonitoringFlowRepository monitoringFlowRepository;
    private final IntraMartMonitoringService monitoringService;

    public List<MonitoringFlowDataView> listFlowData(List<String> flowIds, String imSessionId) {
        String orgId = TenantContext.orgId();
        if (orgId != null) {
            List<MonitoringFlowDataView> fromDb = monitoringFlowRepository.list(orgId, flowIds);
            if (!fromDb.isEmpty()) {
                return fromDb;
            }
        }

        var result = monitoringService.getMonitoringFlowDataList(
                imSessionId, new MonitoringFlowDataSearchCondition(flowIds));
        if (!result.success()) {
            return List.of();
        }
        return result.data().stream().map(MonitoringFlowService::toView).toList();
    }

    private static MonitoringFlowDataView toView(MonitoringFlowData row) {
        return new MonitoringFlowDataView(
                row.flowId(),
                row.flowName(),
                row.approveCount(),
                row.approveEndCount(),
                row.denyCount(),
                row.discontinueCount(),
                row.matterHandleCount(),
                row.minimumTime(),
                row.maximumTime(),
                row.averageTime(),
                row.amountTime(),
                row.countSum());
    }
}
