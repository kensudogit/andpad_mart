package jp.andpad.imart.stamp.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 印影フレームノード（{@code StampFrameNodeInfo} 相当）。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/StampFrameNodeInfo/index.html">StampFrameNodeInfo</a>
 */
public record StampFrameNode(
        String nodeId, String nodeName, String nodeType, String parentNode, String routeTraceId) {

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nodeId", nodeId);
        map.put("nodeName", nodeName);
        map.put("nodeType", nodeType);
        map.put("parentNode", parentNode);
        map.put("routeTraceId", routeTraceId);
        return map;
    }

    public static StampFrameNode fromMap(Map<String, Object> map) {
        return new StampFrameNode(
                stringVal(map.get("nodeId")),
                stringVal(map.get("nodeName")),
                stringVal(map.get("nodeType")),
                stringVal(map.get("parentNode")),
                stringVal(map.get("routeTraceId")));
    }

    private static String stringVal(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
