package jp.andpad.imart.stamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.model.StampFrameNode;
import jp.andpad.imart.stamp.model.WorkflowStampResult;

/** CplMatterStampList ブリッジ応答の変換ユーティリティ。 */
public final class StampSupport {

    private StampSupport() {}

    @SuppressWarnings("unchecked")
    public static WorkflowStampResult mapWorkflowResult(Object result) {
        if (!(result instanceof Map<?, ?> map)) {
            return WorkflowStampResult.fail("unexpected stamp response");
        }
        Object error = map.get("error");
        Object success = map.get("success");
        if (success instanceof Boolean ok && !ok) {
            return WorkflowStampResult.fail(error != null ? String.valueOf(error) : "stamp call failed");
        }
        Object count = map.get("count");
        if (count instanceof Number number) {
            return WorkflowStampResult.okCount(number.intValue());
        }
        Object data = map.get("data");
        if (data == null) {
            return WorkflowStampResult.ok();
        }
        if (data instanceof Number number) {
            return WorkflowStampResult.okCount(number.intValue());
        }
        if (data instanceof List<?> list) {
            if (list.isEmpty()) {
                return WorkflowStampResult.ok();
            }
            Object first = list.get(0);
            if (first instanceof Map<?, ?> firstMap && firstMap.containsKey("nodeName")) {
                return WorkflowStampResult.okFrames(parseFrames(list));
            }
            return WorkflowStampResult.ok(parseStamps(list));
        }
        if (data instanceof Map<?, ?> dataMap) {
            Object stampRows = dataMap.get("stampList");
            if (stampRows instanceof List<?> list) {
                return WorkflowStampResult.ok(parseStamps(list));
            }
            Object frameRows = dataMap.get("stampFrameList");
            if (frameRows instanceof List<?> list) {
                return WorkflowStampResult.okFrames(parseFrames(list));
            }
            if (dataMap.containsKey("nodeName")) {
                return WorkflowStampResult.okFrames(List.of(StampFrameNode.fromMap((Map<String, Object>) dataMap)));
            }
            return WorkflowStampResult.ok(List.of(MatterStampData.fromMap((Map<String, Object>) dataMap)));
        }
        return WorkflowStampResult.ok();
    }

    private static List<MatterStampData> parseStamps(List<?> list) {
        List<MatterStampData> rows = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> rowMap) {
                rows.add(MatterStampData.fromMap((Map<String, Object>) rowMap));
            }
        }
        return rows;
    }

    private static List<StampFrameNode> parseFrames(List<?> list) {
        List<StampFrameNode> rows = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> rowMap) {
                rows.add(StampFrameNode.fromMap((Map<String, Object>) rowMap));
            }
        }
        return rows;
    }
}
