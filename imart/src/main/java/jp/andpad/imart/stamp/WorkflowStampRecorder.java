package jp.andpad.imart.stamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.model.StampFrameNode;
import jp.andpad.imart.stamp.stub.DevIntraMartStampService;
import jp.andpad.imart.stamp.stub.InMemoryMatterStampStore;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ワークフロー完了時に {@code CplMatterStampList} 相当の印影データを登録する。
 *
 * <p>スタブモードでは {@link DevIntraMartStampService} へ印影を蓄積し、
 * HTTP モードでは IM 側の完了案件印影を参照する。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStampRecorder {

    private static final DateTimeFormatter PROCESS_DATE =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final IntraMartStampProperties properties;
    private final InMemoryMatterStampStore store;
    private final MatterStampPersister persister;
    private final MatterStampFileWriter fileWriter;

    /**
     * ワークフロー終了時に印影データを生成する。
     *
     * <p>終了ステータス（承認・却下・取消）到達時のみ印影を登録する。
     */
    public void recordCompletion(
            WorkflowDefinition definition,
            String instanceId,
            String entityType,
            String entityId,
            WorkflowAction action,
            WorkflowInstanceStatus nextStatus,
            String stepKey,
            String actorName,
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
        String stampType = resolveStampType(nextStatus, action);
        String nodeId = stepKey != null ? stepKey : "complete";
        String nodeName = resolveNodeName(definition, nodeId);

        MatterStampData stamp = new MatterStampData(
                systemMatterId,
                store.nextNo(),
                nodeId,
                PROCESS_DATE.format(LocalDateTime.now()),
                action != null ? action.name() : "COMPLETE",
                actorName != null ? actorName : "system",
                "user",
                nodeName,
                "node",
                stampType,
                "type",
                stampType,
                "0",
                definition.flowId(),
                entityType,
                entityId,
                instanceId);

        store.addStamp(stamp);
        persister.persist(stamp);
        fileWriter.append("recordCompletion", List.of(stamp));

        ensureDefaultFrames(systemMatterId, definition);

        log.info(
                "workflow stamp recorded: flowId={} systemMatterId={} stampType={}",
                definition.flowId(),
                systemMatterId,
                stampType);
    }

    private void ensureDefaultFrames(String systemMatterId, WorkflowDefinition definition) {
        if (!store.listFrames(systemMatterId).isEmpty()) {
            return;
        }
        List<StampFrameNode> frames = definition.steps().stream()
                .map(step -> new StampFrameNode(
                        step.stepKey(), step.name(), step.stepType().name(), null, step.stepKey()))
                .toList();
        store.setFrames(systemMatterId, frames);
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

    private static String resolveNodeName(WorkflowDefinition definition, String nodeId) {
        try {
            return definition.stepByKey(nodeId).name();
        } catch (IllegalArgumentException ex) {
            return nodeId;
        }
    }

    private static String resolveStampType(WorkflowInstanceStatus status, WorkflowAction action) {
        if (status == WorkflowInstanceStatus.REJECTED) {
            return "deny";
        }
        if (status == WorkflowInstanceStatus.CANCELLED) {
            return "discontinue";
        }
        if (action == WorkflowAction.APPROVE) {
            return "approve";
        }
        return "approveEnd";
    }
}
