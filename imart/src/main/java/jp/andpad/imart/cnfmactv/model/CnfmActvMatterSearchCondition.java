package jp.andpad.imart.cnfmactv.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 確認案件検索条件（{@code ListSearchCondition} 相当）。 */
public record CnfmActvMatterSearchCondition(List<String> flowIds, Integer limit) {

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("flowId", flowIds != null ? flowIds : List.of());
        if (limit != null) {
            map.put("limit", limit);
        }
        return map;
    }
}
