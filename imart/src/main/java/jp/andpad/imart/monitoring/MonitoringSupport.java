package jp.andpad.imart.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;

/** MonitoringManager ブリッジ応答の変換ユーティリティ。 */
public final class MonitoringSupport {

    private MonitoringSupport() {}

    @SuppressWarnings("unchecked")
    public static WorkflowMonitoringResult mapWorkflowResult(Object result) {
        if (!(result instanceof Map<?, ?> map)) {
            return WorkflowMonitoringResult.fail("unexpected monitoring response");
        }
        Object error = map.get("error");
        Object success = map.get("success");
        if (success instanceof Boolean ok && !ok) {
            return WorkflowMonitoringResult.fail(error != null ? String.valueOf(error) : "monitoring call failed");
        }
        Object data = map.get("data");
        if (data == null) {
            return WorkflowMonitoringResult.ok();
        }
        if (data instanceof List<?> list) {
            List<MonitoringFlowData> rows = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> rowMap) {
                    rows.add(MonitoringFlowData.fromMap((Map<String, Object>) rowMap));
                }
            }
            return WorkflowMonitoringResult.ok(rows);
        }
        if (data instanceof Map<?, ?> dataMap) {
            Object rows = dataMap.get("monitoringFlowDataList");
            if (rows instanceof List<?> list) {
                List<MonitoringFlowData> parsed = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?> rowMap) {
                        parsed.add(MonitoringFlowData.fromMap((Map<String, Object>) rowMap));
                    }
                }
                return WorkflowMonitoringResult.ok(parsed);
            }
            return WorkflowMonitoringResult.ok(List.of(MonitoringFlowData.fromMap((Map<String, Object>) dataMap)));
        }
        return WorkflowMonitoringResult.ok();
    }

    public static String positiveCount(long value) {
        return String.valueOf(Math.max(1L, value));
    }
}
