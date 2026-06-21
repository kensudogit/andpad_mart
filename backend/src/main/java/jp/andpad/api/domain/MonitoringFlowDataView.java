package jp.andpad.api.domain;

/** フロー別モニタリング情報（GraphQL / REST 用）。 */
public record MonitoringFlowDataView(
        String flowId,
        String flowName,
        String approveCount,
        String approveEndCount,
        String denyCount,
        String discontinueCount,
        String matterHandleCount,
        String minimumTime,
        String maximumTime,
        String averageTime,
        String amountTime,
        String countSum) {}
