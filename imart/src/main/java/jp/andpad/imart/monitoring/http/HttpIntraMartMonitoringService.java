package jp.andpad.imart.monitoring.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import jp.andpad.imart.monitoring.MonitoringFlowFileWriter;
import jp.andpad.imart.monitoring.MonitoringFlowPersister;
import jp.andpad.imart.monitoring.MonitoringSupport;
import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;
import jp.andpad.imart.monitoring.spi.IntraMartMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** HTTP ブリッジ経由の {@code MonitoringManager} 実装。 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.monitoring.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartMonitoringService implements IntraMartMonitoringService {

    private final IntraMartSsjsBridgeClient bridgeClient;
    private final MonitoringFlowFileWriter fileWriter;
    private final MonitoringFlowPersister persister;

    @Override
    public WorkflowMonitoringResult createMonitoringFlowData(String sessionId, List<MonitoringFlowData> models) {
        WorkflowMonitoringResult result =
                invoke("createMonitoringFlowData", List.of(toModelMaps(models)), sessionId);
        if (result.success()) {
            fileWriter.append("createMonitoringFlowData", models);
            models.forEach(persister::persist);
        }
        return result;
    }

    @Override
    public WorkflowMonitoringResult updateMonitoringFlowData(String sessionId, List<MonitoringFlowData> models) {
        WorkflowMonitoringResult result =
                invoke("updateMonitoringFlowData", List.of(toModelMaps(models)), sessionId);
        if (result.success()) {
            fileWriter.append("updateMonitoringFlowData", models);
            models.forEach(persister::persist);
        }
        return result;
    }

    @Override
    public WorkflowMonitoringResult getMonitoringFlowDataList(
            String sessionId, MonitoringFlowDataSearchCondition condition) {
        MonitoringFlowDataSearchCondition resolved =
                condition != null ? condition : new MonitoringFlowDataSearchCondition(List.of());
        return invoke("getMonitoringFlowDataList", List.of(resolved.toMap()), sessionId);
    }

    private WorkflowMonitoringResult invoke(String method, List<Object> args, String sessionId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "im_workflow", "MonitoringManager", method, args, sessionId, Map.of()));
        if (!response.success()) {
            log.warn("MonitoringManager.{} failed: {}", method, response.error());
            return WorkflowMonitoringResult.fail(response.error());
        }
        return MonitoringSupport.mapWorkflowResult(response.result());
    }

    private static List<Map<String, Object>> toModelMaps(List<MonitoringFlowData> models) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (MonitoringFlowData model : models) {
            maps.add(model.toMap());
        }
        return maps;
    }
}
