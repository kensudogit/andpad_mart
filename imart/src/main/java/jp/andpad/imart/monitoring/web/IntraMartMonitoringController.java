package jp.andpad.imart.monitoring.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;
import jp.andpad.imart.monitoring.spi.IntraMartMonitoringService;
import lombok.RequiredArgsConstructor;

/**
 * intra-mart ワークフローモニタリング REST API（{@code MonitoringManager} 連携）。
 */
@RestController
@RequestMapping("/auth/imart/monitoring")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.monitoring.enabled", havingValue = "true", matchIfMissing = true)
public class IntraMartMonitoringController {

    private final IntraMartMonitoringService monitoringService;

    /** フロー別モニタリング情報を取得する。 */
    @GetMapping("/flows")
    public ResponseEntity<Map<String, Object>> listFlows(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam(required = false) List<String> flowId) {
        WorkflowMonitoringResult result = monitoringService.getMonitoringFlowDataList(
                sessionHeader, new MonitoringFlowDataSearchCondition(flowId));
        return toResponse(result);
    }

    /** フロー別モニタリング情報を新規作成する。 */
    @PostMapping("/flows")
    public ResponseEntity<Map<String, Object>> createFlows(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestBody List<MonitoringFlowBody> body) {
        WorkflowMonitoringResult result = monitoringService.createMonitoringFlowData(
                sessionHeader, body.stream().map(MonitoringFlowBody::toModel).toList());
        return toResponse(result);
    }

    /** フロー別モニタリング情報を更新する。 */
    @PostMapping("/flows/update")
    public ResponseEntity<Map<String, Object>> updateFlows(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestBody List<MonitoringFlowBody> body) {
        WorkflowMonitoringResult result = monitoringService.updateMonitoringFlowData(
                sessionHeader, body.stream().map(MonitoringFlowBody::toModel).toList());
        return toResponse(result);
    }

    private static ResponseEntity<Map<String, Object>> toResponse(WorkflowMonitoringResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("error", result.error());
        body.put("monitoringFlowDataList", result.data().stream().map(MonitoringFlowData::toMap).toList());
        return result.success() ? ResponseEntity.ok(body) : ResponseEntity.badRequest().body(body);
    }

    public record MonitoringFlowBody(
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
            String amountTime) {

        MonitoringFlowData toModel() {
            return new MonitoringFlowData(
                    flowId,
                    flowName,
                    approveCount,
                    approveEndCount,
                    denyCount,
                    discontinueCount,
                    matterHandleCount,
                    minimumTime,
                    maximumTime,
                    averageTime,
                    amountTime,
                    null);
        }
    }
}
