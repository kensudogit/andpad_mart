package jp.andpad.imart.stamp.model;

import java.util.List;

/** {@code WorkflowResultInfo} 相当の印影処理結果。 */
public record WorkflowStampResult(
        boolean success,
        String error,
        List<MatterStampData> stamps,
        Integer count,
        List<StampFrameNode> frames) {

    public static WorkflowStampResult ok() {
        return new WorkflowStampResult(true, null, List.of(), null, List.of());
    }

    public static WorkflowStampResult ok(List<MatterStampData> stamps) {
        return new WorkflowStampResult(true, null, stamps != null ? stamps : List.of(), null, List.of());
    }

    public static WorkflowStampResult okCount(int count) {
        return new WorkflowStampResult(true, null, List.of(), count, List.of());
    }

    public static WorkflowStampResult okFrames(List<StampFrameNode> frames) {
        return new WorkflowStampResult(true, null, List.of(), null, frames != null ? frames : List.of());
    }

    public static WorkflowStampResult fail(String error) {
        return new WorkflowStampResult(false, error, List.of(), null, List.of());
    }
}
