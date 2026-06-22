package jp.andpad.imart.cnfmactv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.model.WorkflowCnfmActvMatterResult;

public final class CnfmActvMatterSupport {

    private CnfmActvMatterSupport() {}

    @SuppressWarnings("unchecked")
    public static WorkflowCnfmActvMatterResult mapWorkflowResult(Object result) {
        if (!(result instanceof Map<?, ?> map)) {
            return WorkflowCnfmActvMatterResult.fail("unexpected cnfm actv matter response");
        }
        Object error = map.get("error");
        Object success = map.get("success");
        if (success instanceof Boolean ok && !ok) {
            return WorkflowCnfmActvMatterResult.fail(error != null ? String.valueOf(error) : "cnfm actv matter call failed");
        }
        Object data = map.get("data");
        if (data == null) {
            return WorkflowCnfmActvMatterResult.ok(List.of());
        }
        if (data instanceof Number number) {
            return WorkflowCnfmActvMatterResult.okCount(number.longValue());
        }
        if (data instanceof List<?> list) {
            return WorkflowCnfmActvMatterResult.ok(parseList(list));
        }
        if (data instanceof Map<?, ?> dataMap) {
            Object rows = dataMap.get("cnfmActvMatterList");
            if (rows instanceof List<?> list) {
                return WorkflowCnfmActvMatterResult.ok(parseList(list));
            }
            return WorkflowCnfmActvMatterResult.ok(List.of(ActvMatterCnfmData.fromMap((Map<String, Object>) dataMap)));
        }
        return WorkflowCnfmActvMatterResult.ok(List.of());
    }

    @SuppressWarnings("unchecked")
    private static List<ActvMatterCnfmData> parseList(List<?> list) {
        List<ActvMatterCnfmData> rows = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> rowMap) {
                rows.add(ActvMatterCnfmData.fromMap((Map<String, Object>) rowMap));
            }
        }
        return rows;
    }
}
