package jp.andpad.imart.asyncprocess.model;

import java.util.List;

/** AsyncProcessWorkflow API 呼び出し結果。 */
public record WorkflowAsyncProcessResult(boolean success, String error, List<AsyncProcessStatusData> data, Long count) {

    public static WorkflowAsyncProcessResult ok(List<AsyncProcessStatusData> data) {
        return new WorkflowAsyncProcessResult(true, null, data, null);
    }

    public static WorkflowAsyncProcessResult ok() {
        return new WorkflowAsyncProcessResult(true, null, List.of(), null);
    }

    public static WorkflowAsyncProcessResult okCount(long count) {
        return new WorkflowAsyncProcessResult(true, null, List.of(), count);
    }

    public static WorkflowAsyncProcessResult fail(String error) {
        return new WorkflowAsyncProcessResult(false, error, List.of(), null);
    }
}
