package jp.andpad.imart.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;
import jp.andpad.imart.monitoring.spi.IntraMartMonitoringService;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ワークフロー完了時に {@code MonitoringManager.createMonitoringFlowData} /
 * {@code updateMonitoringFlowData} を呼び出す。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowMonitoringRecorder {

    private final IntraMartMonitoringProperties properties;
    private final IntraMartMonitoringService monitoringService;

    /**
     * フロー別モニタリング情報が未作成なら新規作成する。
     *
     * <p>初回 SUBMIT 時に {@code createMonitoringFlowData} を呼び出す。
     */
    public void ensureFlowMonitoring(WorkflowDefinition definition, String imSessionId) {
        if (!properties.isEnabled() || !properties.isRecordOnTransition()) {
            return;
        }
        if (!isTargetFlow(definition.flowId())) {
            return;
        }
        if (findExisting(definition.flowId(), imSessionId) != null) {
            return;
        }
        MonitoringFlowData initial = buildInitial(definition, WorkflowInstanceStatus.RUNNING, WorkflowAction.SUBMIT, 1L);
        WorkflowMonitoringResult result =
                monitoringService.createMonitoringFlowData(imSessionId, List.of(initial));
        if (!result.success()) {
            log.warn(
                    "workflow monitoring not created: flowId={} error={}",
                    definition.flowId(),
                    result.error());
        }
    }

    /**
     * ワークフロー遷移後のモニタリング情報を更新する。
     *
     * <p>終了ステータス（承認・却下・取消）到達時のみ集計を更新する。
     */
    public void recordTransition(
            WorkflowDefinition definition,
            String instanceId,
            WorkflowAction action,
            WorkflowInstanceStatus nextStatus,
            String startedAtIso,
            String completedAtIso,
            String imSessionId) {
        if (!properties.isEnabled() || !properties.isRecordOnTransition()) {
            return;
        }
        if (!isTargetFlow(definition.flowId())) {
            return;
        }
        if (!isTerminal(nextStatus)) {
            return;
        }

        long minutes = resolveProcessingMinutes(startedAtIso, completedAtIso);
        MonitoringFlowData existing = findExisting(definition.flowId(), imSessionId);
        MonitoringFlowData next = existing == null
                ? buildInitial(definition, nextStatus, action, minutes)
                : increment(existing, definition, nextStatus, action, minutes);

        WorkflowMonitoringResult result = existing == null
                ? monitoringService.createMonitoringFlowData(imSessionId, List.of(next))
                : monitoringService.updateMonitoringFlowData(imSessionId, List.of(next));

        if (!result.success()) {
            log.warn(
                    "workflow monitoring not recorded: flowId={} instanceId={} error={}",
                    definition.flowId(),
                    instanceId,
                    result.error());
        }
    }

    private boolean isTargetFlow(String flowId) {
        List<String> configured = properties.getFlowIds();
        return configured == null || configured.isEmpty() || configured.contains(flowId);
    }

    private static boolean isTerminal(WorkflowInstanceStatus status) {
        return status == WorkflowInstanceStatus.APPROVED
                || status == WorkflowInstanceStatus.REJECTED
                || status == WorkflowInstanceStatus.CANCELLED;
    }

    private MonitoringFlowData findExisting(String flowId, String imSessionId) {
        WorkflowMonitoringResult result = monitoringService.getMonitoringFlowDataList(
                imSessionId, new MonitoringFlowDataSearchCondition(List.of(flowId)));
        if (!result.success() || result.data().isEmpty()) {
            return null;
        }
        return result.data().get(0);
    }

    private static MonitoringFlowData buildInitial(
            WorkflowDefinition definition,
            WorkflowInstanceStatus status,
            WorkflowAction action,
            long minutes) {
        long approveCount = 1L;
        long approveEndCount = status == WorkflowInstanceStatus.APPROVED ? 1L : 1L;
        long denyCount = status == WorkflowInstanceStatus.REJECTED ? 1L : 1L;
        long discontinueCount = status == WorkflowInstanceStatus.CANCELLED ? 1L : 1L;
        long matterHandleCount = isHandleAction(action) ? 1L : 1L;
        String time = MonitoringSupport.positiveCount(minutes);
        return new MonitoringFlowData(
                definition.flowId(),
                definition.name(),
                MonitoringSupport.positiveCount(approveCount),
                MonitoringSupport.positiveCount(approveEndCount),
                MonitoringSupport.positiveCount(denyCount),
                MonitoringSupport.positiveCount(discontinueCount),
                MonitoringSupport.positiveCount(matterHandleCount),
                time,
                time,
                time,
                time,
                null);
    }

    private static MonitoringFlowData increment(
            MonitoringFlowData existing,
            WorkflowDefinition definition,
            WorkflowInstanceStatus status,
            WorkflowAction action,
            long minutes) {
        long approveCount = parseCount(existing.approveCount()) + 1L;
        long approveEndCount = parseCount(existing.approveEndCount()) + (status == WorkflowInstanceStatus.APPROVED ? 1L : 0L);
        long denyCount = parseCount(existing.denyCount()) + (status == WorkflowInstanceStatus.REJECTED ? 1L : 0L);
        long discontinueCount =
                parseCount(existing.discontinueCount()) + (status == WorkflowInstanceStatus.CANCELLED ? 1L : 0L);
        long matterHandleCount = parseCount(existing.matterHandleCount()) + (isHandleAction(action) ? 1L : 0L);

        long minTime = Math.min(parseCount(existing.minimumTime()), minutes);
        long maxTime = Math.max(parseCount(existing.maximumTime()), minutes);
        long amountTime = parseCount(existing.amountTime()) + minutes;
        long completedCount = Math.max(1L, parseCount(existing.approveCount()) + 1L);
        long averageTime = Math.max(1L, amountTime / completedCount);

        return new MonitoringFlowData(
                definition.flowId(),
                definition.name(),
                MonitoringSupport.positiveCount(approveCount),
                MonitoringSupport.positiveCount(approveEndCount),
                MonitoringSupport.positiveCount(denyCount),
                MonitoringSupport.positiveCount(discontinueCount),
                MonitoringSupport.positiveCount(matterHandleCount),
                MonitoringSupport.positiveCount(minTime),
                MonitoringSupport.positiveCount(maxTime),
                MonitoringSupport.positiveCount(averageTime),
                MonitoringSupport.positiveCount(amountTime),
                existing.countSum());
    }

    private static boolean isHandleAction(WorkflowAction action) {
        return action == WorkflowAction.APPROVE
                || action == WorkflowAction.REJECT
                || action == WorkflowAction.RETURN
                || action == WorkflowAction.CANCEL;
    }

    private static long parseCount(String value) {
        if (value == null || value.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return 1L;
        }
    }

    private static long resolveProcessingMinutes(String startedAtIso, String completedAtIso) {
        try {
            Instant started = startedAtIso != null ? Instant.parse(startedAtIso) : Instant.now();
            Instant completed = completedAtIso != null ? Instant.parse(completedAtIso) : Instant.now();
            long minutes = Duration.between(started, completed).toMinutes();
            return Math.max(1L, minutes);
        } catch (Exception ex) {
            return 1L;
        }
    }
}
