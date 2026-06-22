package jp.andpad.imart.asyncprocess.stub;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.asyncprocess.AsyncProcessFileWriter;
import jp.andpad.imart.asyncprocess.AsyncProcessPersister;
import jp.andpad.imart.asyncprocess.AsyncProcessSupport;
import jp.andpad.imart.asyncprocess.IntraMartAsyncProcessProperties;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;
import jp.andpad.imart.asyncprocess.model.WorkflowAsyncProcessResult;
import jp.andpad.imart.asyncprocess.spi.IntraMartAsyncProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** ローカル開発用 {@code AsyncProcessWorkflow} スタブ実装。 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.asyncprocess.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartAsyncProcessService implements IntraMartAsyncProcessService {

    private static final DateTimeFormatter PROC_DATE =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final IntraMartAsyncProcessProperties properties;
    private final InMemoryAsyncProcessStore store;
    private final AsyncProcessFileWriter fileWriter;
    private final AsyncProcessPersister persister;

    @Override
    public WorkflowAsyncProcessResult createAsyncProcessStatusData(
            String sessionId, List<AsyncProcessStatusData> models) {
        List<AsyncProcessStatusData> saved = new ArrayList<>();
        for (AsyncProcessStatusData model : models) {
            AsyncProcessStatusData normalized = normalize(model);
            if (normalized.acceptId() == null || normalized.acceptId().isBlank()) {
                return WorkflowAsyncProcessResult.fail("acceptId is required");
            }
            if (store.contains(normalized.acceptId())) {
                return WorkflowAsyncProcessResult.fail("async process already exists: " + normalized.acceptId());
            }
            store.put(normalized);
            saved.add(normalized);
            persister.persist(normalized, null, null, null);
            log.info(
                    "[stub-asyncprocess] created acceptId={} flowId={} status={}",
                    normalized.acceptId(),
                    normalized.flowId(),
                    normalized.asyncProcStatus());
        }
        fileWriter.append("createAsyncProcessStatusData", saved);
        return WorkflowAsyncProcessResult.ok(saved);
    }

    @Override
    public WorkflowAsyncProcessResult updateAsyncProcessStatusData(
            String sessionId, List<AsyncProcessStatusData> models) {
        List<AsyncProcessStatusData> saved = new ArrayList<>();
        for (AsyncProcessStatusData model : models) {
            AsyncProcessStatusData normalized = normalize(model);
            if (normalized.acceptId() == null || normalized.acceptId().isBlank()) {
                return WorkflowAsyncProcessResult.fail("acceptId is required");
            }
            if (!store.contains(normalized.acceptId())) {
                return WorkflowAsyncProcessResult.fail("async process not found: " + normalized.acceptId());
            }
            AsyncProcessStatusData merged = merge(store.get(normalized.acceptId()), normalized);
            store.put(merged);
            saved.add(merged);
            persister.persist(merged, null, null, null);
            log.info(
                    "[stub-asyncprocess] updated acceptId={} status={}",
                    merged.acceptId(),
                    merged.asyncProcStatus());
        }
        fileWriter.append("updateAsyncProcessStatusData", saved);
        return WorkflowAsyncProcessResult.ok(saved);
    }

    @Override
    public WorkflowAsyncProcessResult getAsyncProcessStatusDataList(
            String sessionId, AsyncProcessStatusSearchCondition condition) {
        List<AsyncProcessStatusData> rows = filter(condition);
        return WorkflowAsyncProcessResult.ok(applyLimit(rows, condition));
    }

    @Override
    public WorkflowAsyncProcessResult getAsyncProcessStatusDataListCount(
            String sessionId, AsyncProcessStatusSearchCondition condition) {
        return WorkflowAsyncProcessResult.okCount(filter(condition).size());
    }

    private List<AsyncProcessStatusData> filter(AsyncProcessStatusSearchCondition condition) {
        AsyncProcessStatusSearchCondition resolved =
                condition != null ? condition : new AsyncProcessStatusSearchCondition(List.of(), List.of(), null);
        List<AsyncProcessStatusData> rows;
        if (resolved.systemMatterIds() != null && !resolved.systemMatterIds().isEmpty()) {
            rows = store.listBySystemMatterIds(resolved.systemMatterIds());
        } else if (resolved.flowIds() != null && !resolved.flowIds().isEmpty()) {
            rows = store.listByFlowIds(resolved.flowIds());
        } else {
            rows = store.listAll();
        }
        return rows;
    }

    private static List<AsyncProcessStatusData> applyLimit(
            List<AsyncProcessStatusData> rows, AsyncProcessStatusSearchCondition condition) {
        if (condition == null || condition.limit() == null || condition.limit() <= 0) {
            return rows;
        }
        return rows.stream().limit(condition.limit()).toList();
    }

    private AsyncProcessStatusData normalize(AsyncProcessStatusData model) {
        String acceptId = model.acceptId() != null && !model.acceptId().isBlank()
                ? model.acceptId()
                : "accept-" + UUID.randomUUID().toString().substring(0, 8);
        String procDate = model.procDate() != null && !model.procDate().isBlank()
                ? model.procDate()
                : PROC_DATE.format(LocalDateTime.now());
        return new AsyncProcessStatusData(
                acceptId,
                model.asyncProcStatus() != null ? model.asyncProcStatus() : AsyncProcessSupport.Status.RUNNING,
                model.authUserCode(),
                model.executeUserCode(),
                model.flowId(),
                model.matterName(),
                model.matterNumber(),
                model.message(),
                model.nodeId(),
                model.procComment(),
                procDate,
                model.procType(),
                model.queueId() != null ? model.queueId() : "queue-" + acceptId,
                model.subMessage(),
                model.systemMatterId());
    }

    private static AsyncProcessStatusData merge(AsyncProcessStatusData existing, AsyncProcessStatusData update) {
        return new AsyncProcessStatusData(
                existing.acceptId(),
                update.asyncProcStatus() != null ? update.asyncProcStatus() : existing.asyncProcStatus(),
                update.authUserCode() != null ? update.authUserCode() : existing.authUserCode(),
                update.executeUserCode() != null ? update.executeUserCode() : existing.executeUserCode(),
                update.flowId() != null ? update.flowId() : existing.flowId(),
                update.matterName() != null ? update.matterName() : existing.matterName(),
                update.matterNumber() != null ? update.matterNumber() : existing.matterNumber(),
                update.message() != null ? update.message() : existing.message(),
                update.nodeId() != null ? update.nodeId() : existing.nodeId(),
                update.procComment() != null ? update.procComment() : existing.procComment(),
                update.procDate() != null ? update.procDate() : existing.procDate(),
                update.procType() != null ? update.procType() : existing.procType(),
                update.queueId() != null ? update.queueId() : existing.queueId(),
                update.subMessage() != null ? update.subMessage() : existing.subMessage(),
                update.systemMatterId() != null ? update.systemMatterId() : existing.systemMatterId());
    }
}
