package jp.andpad.imart.stamp.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 完了案件の印影データ（{@code StampDataInfo} / {@code StampDataModel} 相当）。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/StampDataInfo/index.html">StampDataInfo</a>
 */
public record MatterStampData(
        String systemMatterId,
        String no,
        String nodeId,
        String processDate,
        String processId,
        String stampStr1,
        String stampStr1Type,
        String stampStr2,
        String stampStr2Type,
        String stampStr3,
        String stampStr3Type,
        String stampType,
        String cancelFlag,
        String flowId,
        String entityType,
        String entityId,
        String workflowInstanceId) {

    /** IM API 互換の Map 表現。 */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("systemMatterId", systemMatterId);
        map.put("no", no);
        map.put("nodeId", nodeId);
        map.put("processDate", processDate);
        map.put("processId", processId);
        map.put("stampStr1", stampStr1);
        map.put("stampStr1Type", stampStr1Type);
        map.put("stampStr2", stampStr2);
        map.put("stampStr2Type", stampStr2Type);
        map.put("stampStr3", stampStr3);
        map.put("stampStr3Type", stampStr3Type);
        map.put("stampType", stampType);
        map.put("cancelFlag", cancelFlag);
        map.put("flowId", flowId);
        map.put("entityType", entityType);
        map.put("entityId", entityId);
        map.put("workflowInstanceId", workflowInstanceId);
        return map;
    }

    public static MatterStampData fromMap(Map<String, Object> map) {
        return new MatterStampData(
                stringVal(map.get("systemMatterId")),
                stringVal(map.get("no")),
                stringVal(map.get("nodeId")),
                stringVal(map.get("processDate")),
                stringVal(map.get("processId")),
                stringVal(map.get("stampStr1")),
                stringVal(map.get("stampStr1Type")),
                stringVal(map.get("stampStr2")),
                stringVal(map.get("stampStr2Type")),
                stringVal(map.get("stampStr3")),
                stringVal(map.get("stampStr3Type")),
                stringVal(map.get("stampType")),
                stringVal(map.get("cancelFlag")),
                stringVal(map.get("flowId")),
                stringVal(map.get("entityType")),
                stringVal(map.get("entityId")),
                stringVal(map.get("workflowInstanceId")));
    }

    private static String stringVal(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
