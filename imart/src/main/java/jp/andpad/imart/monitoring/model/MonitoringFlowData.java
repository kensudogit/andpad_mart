package jp.andpad.imart.monitoring.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * フロー別モニタリング情報（{@code MonitoringFlowDataInfo} / {@code MonitoringFlowDataModel} 相当）。
 */
public record MonitoringFlowData(
        String flowId,
        String flowName,
        String approveCount,
        String approveEndCount,
        String denyCount,
        String discontinueCount,
        String matterHandleCount,
        String minimumTime,
        String maximumTime,
        String averageTime,
        String amountTime,
        String countSum) {

    /** IM API 互換の Map 表現。 */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("flowId", flowId);
        map.put("flowName", flowName);
        map.put("approveCount", approveCount);
        map.put("approveEndCount", approveEndCount);
        map.put("denyCount", denyCount);
        map.put("discontinueCount", discontinueCount);
        map.put("matterHandleCount", matterHandleCount);
        map.put("minimumTime", minimumTime);
        map.put("maximumTime", maximumTime);
        map.put("averageTime", averageTime);
        map.put("amountTime", amountTime);
        map.put("countSum", countSum);
        return map;
    }

    public static MonitoringFlowData fromMap(Map<String, Object> map) {
        return new MonitoringFlowData(
                stringVal(map.get("flowId")),
                stringVal(map.get("flowName")),
                stringVal(map.get("approveCount")),
                stringVal(map.get("approveEndCount")),
                stringVal(map.get("denyCount")),
                stringVal(map.get("discontinueCount")),
                stringVal(map.get("matterHandleCount")),
                stringVal(map.get("minimumTime")),
                stringVal(map.get("maximumTime")),
                stringVal(map.get("averageTime")),
                stringVal(map.get("amountTime")),
                stringVal(map.get("countSum")));
    }

    private static String stringVal(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
