package jp.andpad.imart.stamp.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 印影一覧検索条件（{@code StampListSearchCondition} 相当）。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/StampListSearchCondition/index.html">StampListSearchCondition</a>
 */
public record StampListSearchCondition(
        List<StampSearchFilter> conditions,
        List<StampSearchOrder> orders,
        Boolean andCombination,
        String count,
        String offset) {

    public StampListSearchCondition {
        conditions = conditions != null ? List.copyOf(conditions) : List.of();
        orders = orders != null ? List.copyOf(orders) : List.of();
    }

    public static StampListSearchCondition empty() {
        return new StampListSearchCondition(List.of(), List.of(), true, null, null);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Map<String, Object>> conditionMaps = new ArrayList<>();
        for (StampSearchFilter condition : conditions) {
            conditionMaps.add(condition.toMap());
        }
        map.put("conditions", conditionMaps);
        List<Map<String, Object>> orderMaps = new ArrayList<>();
        for (StampSearchOrder order : orders) {
            orderMaps.add(order.toMap());
        }
        map.put("orders", orderMaps);
        map.put("andCombination", andCombination);
        map.put("count", count);
        map.put("offset", offset);
        return map;
    }

    public record StampSearchFilter(String column, String value, String operatorType) {
        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("column", column);
            map.put("value", value);
            map.put("operatorType", operatorType);
            return map;
        }
    }

    public record StampSearchOrder(String column, boolean ascending) {
        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("column", column);
            map.put("isASC", ascending);
            return map;
        }
    }
}
