package jp.andpad.imart.monitoring.model;

import java.util.List;

/** {@code WorkflowResultInfo} 相当の処理結果。 */
public record WorkflowMonitoringResult(boolean success, String error, List<MonitoringFlowData> data) {

    public static WorkflowMonitoringResult ok() {
        return new WorkflowMonitoringResult(true, null, List.of());
    }

    public static WorkflowMonitoringResult ok(List<MonitoringFlowData> data) {
        return new WorkflowMonitoringResult(true, null, data != null ? data : List.of());
    }

    public static WorkflowMonitoringResult fail(String error) {
        return new WorkflowMonitoringResult(false, error, List.of());
    }
}
