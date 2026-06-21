package jp.andpad.imart.monitoring.stub;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.monitoring.IntraMartMonitoringProperties;
import jp.andpad.imart.monitoring.MonitoringFlowFileWriter;
import jp.andpad.imart.monitoring.MonitoringFlowPersister;
import jp.andpad.imart.monitoring.MonitoringSupport;
import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;
import jp.andpad.imart.monitoring.spi.IntraMartMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ローカル開発用 {@code MonitoringManager} スタブ実装。
 *
 * <p>IM サーバー無しでフロー別モニタリング情報の作成・更新・参照を行う。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.monitoring.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartMonitoringService implements IntraMartMonitoringService {

    private final IntraMartMonitoringProperties properties;
    private final InMemoryMonitoringFlowStore store;
    private final MonitoringFlowFileWriter fileWriter;
    private final MonitoringFlowPersister persister;

    @Override
    public WorkflowMonitoringResult createMonitoringFlowData(String sessionId, List<MonitoringFlowData> models) {
        List<MonitoringFlowData> saved = new ArrayList<>();
        for (MonitoringFlowData model : models) {
            if (model.flowId() == null || model.flowId().isBlank()) {
                return WorkflowMonitoringResult.fail("flowId is required");
            }
            if (store.contains(model.flowId())) {
                return WorkflowMonitoringResult.fail("monitoring flow data already exists: " + model.flowId());
            }
            MonitoringFlowData normalized = normalize(model);
            store.put(normalized);
            saved.add(normalized);
            persister.persist(normalized);
            log.info(
                    "[stub-monitoring] created flowId={} approveCount={} approveEndCount={}",
                    normalized.flowId(),
                    normalized.approveCount(),
                    normalized.approveEndCount());
        }
        fileWriter.append("createMonitoringFlowData", saved);
        return WorkflowMonitoringResult.ok(saved);
    }

    @Override
    public WorkflowMonitoringResult updateMonitoringFlowData(String sessionId, List<MonitoringFlowData> models) {
        List<MonitoringFlowData> saved = new ArrayList<>();
        for (MonitoringFlowData model : models) {
            if (model.flowId() == null || model.flowId().isBlank()) {
                return WorkflowMonitoringResult.fail("flowId is required");
            }
            if (!store.contains(model.flowId())) {
                return WorkflowMonitoringResult.fail("monitoring flow data not found: " + model.flowId());
            }
            MonitoringFlowData normalized = normalize(model);
            store.put(normalized);
            saved.add(normalized);
            persister.persist(normalized);
            log.info(
                    "[stub-monitoring] updated flowId={} approveCount={} approveEndCount={}",
                    normalized.flowId(),
                    normalized.approveCount(),
                    normalized.approveEndCount());
        }
        fileWriter.append("updateMonitoringFlowData", saved);
        return WorkflowMonitoringResult.ok(saved);
    }

    @Override
    public WorkflowMonitoringResult getMonitoringFlowDataList(
            String sessionId, MonitoringFlowDataSearchCondition condition) {
        List<String> flowIds = condition != null && condition.flowIds() != null ? condition.flowIds() : List.of();
        List<MonitoringFlowData> rows = new ArrayList<>();
        if (flowIds.isEmpty()) {
            for (MonitoringFlowData row : store.listAll()) {
                rows.add(withCountSum(row));
            }
            return WorkflowMonitoringResult.ok(rows);
        }
        for (String flowId : flowIds) {
            MonitoringFlowData row = store.get(flowId);
            if (row != null) {
                rows.add(withCountSum(row));
            }
        }
        return WorkflowMonitoringResult.ok(rows);
    }

    private MonitoringFlowData normalize(MonitoringFlowData model) {
        return new MonitoringFlowData(
                model.flowId(),
                model.flowName(),
                MonitoringSupport.positiveCount(parseLong(model.approveCount())),
                MonitoringSupport.positiveCount(parseLong(model.approveEndCount())),
                MonitoringSupport.positiveCount(parseLong(model.denyCount())),
                MonitoringSupport.positiveCount(parseLong(model.discontinueCount())),
                MonitoringSupport.positiveCount(parseLong(model.matterHandleCount())),
                MonitoringSupport.positiveCount(parseLong(model.minimumTime())),
                MonitoringSupport.positiveCount(parseLong(model.maximumTime())),
                MonitoringSupport.positiveCount(parseLong(model.averageTime())),
                MonitoringSupport.positiveCount(parseLong(model.amountTime())),
                model.countSum());
    }

    private static MonitoringFlowData withCountSum(MonitoringFlowData row) {
        long sum = parseLong(row.approveCount())
                + parseLong(row.approveEndCount())
                + parseLong(row.denyCount())
                + parseLong(row.discontinueCount())
                + parseLong(row.matterHandleCount());
        return new MonitoringFlowData(
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
                String.valueOf(Math.max(1L, sum)));
    }

    private static long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return 1L;
        }
    }
}
