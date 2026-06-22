package jp.andpad.imart.cnfmactv.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 未完了案件確認情報（{@code ActvMatterCnfmInfo} 相当）。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/ActvMatterCnfmInfo/index.html">ActvMatterCnfmInfo</a>
 */
public record ActvMatterCnfmData(
        String systemMatterId,
        String flowId,
        String flowName,
        String matterName,
        String matterNumber,
        String nodeId,
        String applyAuthUserCode,
        String applyAuthUserName,
        String applyDate,
        String arrivedDate,
        String confirmCplFlag,
        String priorityLevel,
        String listType) {

    public static final String LIST_CONFIRM = "CONFIRM";
    public static final String LIST_LUMP_CONFIRM = "LUMP_CONFIRM";

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("systemMatterId", systemMatterId);
        map.put("flowId", flowId);
        map.put("flowName", flowName);
        map.put("matterName", matterName);
        map.put("matterNumber", matterNumber);
        map.put("nodeId", nodeId);
        map.put("applyAuthUserCode", applyAuthUserCode);
        map.put("applyAuthUserName", applyAuthUserName);
        map.put("applyDate", applyDate);
        map.put("arrivedDate", arrivedDate);
        map.put("confirmCplFlag", confirmCplFlag);
        map.put("priorityLevel", priorityLevel);
        return map;
    }

    public static ActvMatterCnfmData fromMap(Map<String, Object> map) {
        return new ActvMatterCnfmData(
                stringVal(map.get("systemMatterId")),
                stringVal(map.get("flowId")),
                stringVal(map.get("flowName")),
                stringVal(map.get("matterName")),
                stringVal(map.get("matterNumber")),
                stringVal(map.get("nodeId")),
                stringVal(map.get("applyAuthUserCode")),
                stringVal(map.get("applyAuthUserName")),
                stringVal(map.get("applyDate")),
                stringVal(map.get("arrivedDate")),
                stringVal(map.get("confirmCplFlag")),
                stringVal(map.get("priorityLevel")),
                stringVal(map.get("listType")));
    }

    private static String stringVal(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
