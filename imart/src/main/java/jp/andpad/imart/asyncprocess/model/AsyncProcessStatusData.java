package jp.andpad.imart.asyncprocess.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 非同期処理状況情報（{@code AsyncProcessStatusDataInfo} 相当）。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/AsyncProcessWorkflow/index.html">AsyncProcessWorkflow</a>
 */
public record AsyncProcessStatusData(
        String acceptId,
        String asyncProcStatus,
        String authUserCode,
        String executeUserCode,
        String flowId,
        String matterName,
        String matterNumber,
        String message,
        String nodeId,
        String procComment,
        String procDate,
        String procType,
        String queueId,
        String subMessage,
        String systemMatterId) {

    /** IM API 互換の Map 表現。 */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("acceptId", acceptId);
        map.put("asyncProcStatus", asyncProcStatus);
        map.put("authUserCode", authUserCode);
        map.put("executeUserCode", executeUserCode);
        map.put("flowId", flowId);
        map.put("matterName", matterName);
        map.put("matterNumber", matterNumber);
        map.put("message", message);
        map.put("nodeId", nodeId);
        map.put("procComment", procComment);
        map.put("procDate", procDate);
        map.put("procType", procType);
        map.put("queueId", queueId);
        map.put("subMessage", subMessage);
        map.put("systemMatterId", systemMatterId);
        return map;
    }

    public static AsyncProcessStatusData fromMap(Map<String, Object> map) {
        return new AsyncProcessStatusData(
                stringVal(map.get("acceptId")),
                stringVal(map.get("asyncProcStatus")),
                stringVal(map.get("authUserCode")),
                stringVal(map.get("executeUserCode")),
                stringVal(map.get("flowId")),
                stringVal(map.get("matterName")),
                stringVal(map.get("matterNumber")),
                stringVal(map.get("message")),
                stringVal(map.get("nodeId")),
                stringVal(map.get("procComment")),
                stringVal(map.get("procDate")),
                stringVal(map.get("procType")),
                stringVal(map.get("queueId")),
                stringVal(map.get("subMessage")),
                stringVal(map.get("systemMatterId")));
    }

    private static String stringVal(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
