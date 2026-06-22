package jp.andpad.imart.asyncprocess.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 非同期処理状況情報検索条件（{@code ListSearchConditionNoMatterProperty} 相当）。 */
public record AsyncProcessStatusSearchCondition(
        List<String> flowIds, List<String> systemMatterIds, Integer limit) {

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("flowId", flowIds != null ? flowIds : List.of());
        map.put("systemMatterId", systemMatterIds != null ? systemMatterIds : List.of());
        if (limit != null) {
            map.put("limit", limit);
        }
        return map;
    }
}
