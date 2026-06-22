package jp.andpad.imart.asyncprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.WorkflowAsyncProcessResult;

/** AsyncProcessWorkflow ブリッジ応答の変換ユーティリティ。 */
public final class AsyncProcessSupport {

    private AsyncProcessSupport() {}

    @SuppressWarnings("unchecked")
    public static WorkflowAsyncProcessResult mapWorkflowResult(Object result) {
        if (!(result instanceof Map<?, ?> map)) {
            return WorkflowAsyncProcessResult.fail("unexpected async process response");
        }
        Object error = map.get("error");
        Object success = map.get("success");
        if (success instanceof Boolean ok && !ok) {
            return WorkflowAsyncProcessResult.fail(error != null ? String.valueOf(error) : "async process call failed");
        }
        Object data = map.get("data");
        if (data == null) {
            return WorkflowAsyncProcessResult.ok();
        }
        if (data instanceof Number number) {
            return WorkflowAsyncProcessResult.okCount(number.longValue());
        }
        if (data instanceof List<?> list) {
            return WorkflowAsyncProcessResult.ok(parseList(list));
        }
        if (data instanceof Map<?, ?> dataMap) {
            Object rows = dataMap.get("asyncProcessStatusDataList");
            if (rows instanceof List<?> list) {
                return WorkflowAsyncProcessResult.ok(parseList(list));
            }
            return WorkflowAsyncProcessResult.ok(List.of(AsyncProcessStatusData.fromMap((Map<String, Object>) dataMap)));
        }
        return WorkflowAsyncProcessResult.ok();
    }

    @SuppressWarnings("unchecked")
    private static List<AsyncProcessStatusData> parseList(List<?> list) {
        List<AsyncProcessStatusData> rows = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> rowMap) {
                rows.add(AsyncProcessStatusData.fromMap((Map<String, Object>) rowMap));
            }
        }
        return rows;
    }

    /** IM AsyncProcessStatus 相当のステータスコード。 */
    public static final class Status {
        public static final String ACCEPTED_BEFORE = "0";
        public static final String ACCEPTED_FAIL = "1";
        public static final String RUNNING = "2";
        public static final String SUCCESS = "3";
        public static final String ERROR = "4";

        private Status() {}
    }
}
