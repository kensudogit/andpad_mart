package jp.andpad.imart.asyncprocess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;
import jp.andpad.imart.asyncprocess.spi.IntraMartAsyncProcessService;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ワークフロー遷移時に {@code AsyncProcessWorkflow.createAsyncProcessStatusData} /
 * {@code updateAsyncProcessStatusData} を呼び出す。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowAsyncProcessRecorder {

    private static final DateTimeFormatter PROC_DATE =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final IntraMartAsyncProcessProperties properties;
    private final IntraMartAsyncProcessService asyncProcessService;
    private final AsyncProcessPersister persister;

    /** WF 起票時に非同期処理状況を RUNNING で登録する。 */
    public void recordSubmit(
            WorkflowDefinition definition,
            String instanceId,
            String entityType,
            String entityId,
            String title,
            String actorUserCode,
            String imSystemMatterId,
            String imSessionId) {
        if (!properties.isEnabled() || !properties.isRecordOnTransition()) {
            return;
        }
        if (!isTargetFlow(definition.flowId())) {
            return;
        }
        String systemMatterId = resolveSystemMatterId(instanceId, imSystemMatterId);
        if (findExisting(systemMatterId, imSessionId) != null) {
            return;
        }
        AsyncProcessStatusData initial = buildStatus(
                "accept-" + instanceId,
                AsyncProcessSupport.Status.RUNNING,
                definition.flowId(),
                title,
                systemMatterId,
                "submit",
                WorkflowAction.SUBMIT.name(),
                actorUserCode,
                "ワークフロー起票",
                null);
        var result = asyncProcessService.createAsyncProcessStatusData(imSessionId, List.of(initial));
        if (!result.success()) {
            log.warn(
                    "async process not created: flowId={} instanceId={} error={}",
                    definition.flowId(),
                    instanceId,
                    result.error());
            return;
        }
        persister.persist(initial, instanceId, entityType, entityId);
    }

    /** WF 完了時に非同期処理状況を SUCCESS / ERROR で更新する。 */
    public void recordCompletion(
            WorkflowDefinition definition,
            String instanceId,
            String entityType,
            String entityId,
            String title,
            WorkflowAction action,
            WorkflowInstanceStatus nextStatus,
            String stepKey,
            String actorUserCode,
            String imSystemMatterId,
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

        String systemMatterId = resolveSystemMatterId(instanceId, imSystemMatterId);
        String acceptId = "accept-" + instanceId;
        String status = nextStatus == WorkflowInstanceStatus.APPROVED
                ? AsyncProcessSupport.Status.SUCCESS
                : AsyncProcessSupport.Status.ERROR;
        String message = nextStatus == WorkflowInstanceStatus.APPROVED ? "処理完了" : "処理失敗";

        AsyncProcessStatusData existing = findExisting(systemMatterId, imSessionId);
        AsyncProcessStatusData next = buildStatus(
                existing != null ? existing.acceptId() : acceptId,
                status,
                definition.flowId(),
                title,
                systemMatterId,
                stepKey,
                action != null ? action.name() : "COMPLETE",
                actorUserCode,
                message,
                existing);

        var result = existing == null
                ? asyncProcessService.createAsyncProcessStatusData(imSessionId, List.of(next))
                : asyncProcessService.updateAsyncProcessStatusData(imSessionId, List.of(next));
        if (!result.success()) {
            log.warn(
                    "async process not recorded: flowId={} instanceId={} error={}",
                    definition.flowId(),
                    instanceId,
                    result.error());
            return;
        }
        persister.persist(next, instanceId, entityType, entityId);
    }

    private AsyncProcessStatusData findExisting(String systemMatterId, String imSessionId) {
        var result = asyncProcessService.getAsyncProcessStatusDataList(
                imSessionId, new AsyncProcessStatusSearchCondition(List.of(), List.of(systemMatterId), 1));
        if (!result.success() || result.data().isEmpty()) {
            return null;
        }
        return result.data().get(0);
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

    private static String resolveSystemMatterId(String instanceId, String imSystemMatterId) {
        if (imSystemMatterId != null && !imSystemMatterId.isBlank()) {
            return imSystemMatterId;
        }
        return "wf-" + instanceId;
    }

    private static AsyncProcessStatusData buildStatus(
            String acceptId,
            String asyncProcStatus,
            String flowId,
            String matterName,
            String systemMatterId,
            String nodeId,
            String procType,
            String executeUserCode,
            String message,
            AsyncProcessStatusData existing) {
        return new AsyncProcessStatusData(
                acceptId,
                asyncProcStatus,
                existing != null ? existing.authUserCode() : executeUserCode,
                executeUserCode,
                flowId,
                matterName,
                existing != null ? existing.matterNumber() : systemMatterId,
                message,
                nodeId,
                existing != null ? existing.procComment() : null,
                PROC_DATE.format(LocalDateTime.now()),
                procType,
                existing != null ? existing.queueId() : "queue-" + acceptId,
                existing != null ? existing.subMessage() : null,
                systemMatterId);
    }
}
