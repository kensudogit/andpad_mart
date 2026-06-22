package jp.andpad.imart.cnfmactv.model;

import java.util.List;

/** CnfmActvMatterList API 呼び出し結果。 */
public record WorkflowCnfmActvMatterResult(
        boolean success, String error, List<ActvMatterCnfmData> data, Long count) {

    public static WorkflowCnfmActvMatterResult ok(List<ActvMatterCnfmData> data) {
        return new WorkflowCnfmActvMatterResult(true, null, data, null);
    }

    public static WorkflowCnfmActvMatterResult okCount(long count) {
        return new WorkflowCnfmActvMatterResult(true, null, List.of(), count);
    }

    public static WorkflowCnfmActvMatterResult fail(String error) {
        return new WorkflowCnfmActvMatterResult(false, error, List.of(), null);
    }
}
