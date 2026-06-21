package jp.andpad.imart.monitoring.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** フロー別モニタリング情報検索条件（{@code MonitoringFlowDataSearchConditionInfo} 相当）。 */
public record MonitoringFlowDataSearchCondition(List<String> flowIds) {

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("flowId", flowIds != null ? flowIds : List.of());
        return map;
    }
}
