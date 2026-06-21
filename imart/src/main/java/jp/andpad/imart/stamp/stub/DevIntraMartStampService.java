package jp.andpad.imart.stamp.stub;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.stamp.IntraMartStampProperties;
import jp.andpad.imart.stamp.MatterStampFileWriter;
import jp.andpad.imart.stamp.MatterStampPersister;
import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.model.StampFrameNode;
import jp.andpad.imart.stamp.model.StampListSearchCondition;
import jp.andpad.imart.stamp.model.WorkflowStampResult;
import jp.andpad.imart.stamp.spi.IntraMartStampService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ローカル開発用 {@code CplMatterStampList} スタブ実装。
 *
 * <p>IM サーバー無しで完了案件印影の参照を行う。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.stamp.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartStampService implements IntraMartStampService {

    private final InMemoryMatterStampStore store;
    private final MatterStampFileWriter fileWriter;
    private final MatterStampPersister persister;

    /** スタブ用に印影データを登録する（WorkflowStampRecorder から利用）。 */
    public void registerStamp(MatterStampData stamp) {
        store.addStamp(stamp);
        persister.persist(stamp);
        fileWriter.append("registerStamp", List.of(stamp));
        log.info(
                "[stub-stamp] registered systemMatterId={} nodeId={} stampType={}",
                stamp.systemMatterId(),
                stamp.nodeId(),
                stamp.stampType());
    }

    /** スタブ用に印影フレームを登録する。 */
    public void registerFrames(String systemMatterId, List<StampFrameNode> frames) {
        store.setFrames(systemMatterId, frames);
    }

    @Override
    public WorkflowStampResult getStampList(
            String sessionId, String systemMatterId, StampListSearchCondition condition) {
        if (systemMatterId == null || systemMatterId.isBlank()) {
            return WorkflowStampResult.fail("systemMatterId is required");
        }
        return WorkflowStampResult.ok(applyCondition(store.listStamps(systemMatterId), condition));
    }

    @Override
    public WorkflowStampResult getStampListCount(
            String sessionId, String systemMatterId, StampListSearchCondition condition) {
        if (systemMatterId == null || systemMatterId.isBlank()) {
            return WorkflowStampResult.fail("systemMatterId is required");
        }
        return WorkflowStampResult.okCount(applyCondition(store.listStamps(systemMatterId), condition).size());
    }

    @Override
    public WorkflowStampResult getStampResultFrameList(
            String sessionId, String systemMatterId, boolean sortType) {
        if (systemMatterId == null || systemMatterId.isBlank()) {
            return WorkflowStampResult.fail("systemMatterId is required");
        }
        List<StampFrameNode> frames = new ArrayList<>(store.listFrames(systemMatterId));
        if (sortType) {
            frames.sort((a, b) -> String.valueOf(a.nodeId()).compareTo(String.valueOf(b.nodeId())));
        }
        return WorkflowStampResult.okFrames(frames);
    }

    private static List<MatterStampData> applyCondition(
            List<MatterStampData> rows, StampListSearchCondition condition) {
        if (condition == null || condition.conditions().isEmpty()) {
            return paginate(rows, condition);
        }
        List<MatterStampData> filtered = new ArrayList<>();
        for (MatterStampData row : rows) {
            if (matches(row, condition)) {
                filtered.add(row);
            }
        }
        return paginate(filtered, condition);
    }

    private static boolean matches(MatterStampData row, StampListSearchCondition condition) {
        boolean and = condition.andCombination() == null || condition.andCombination();
        boolean matched = and;
        for (StampListSearchCondition.StampSearchFilter filter : condition.conditions()) {
            String actual = resolveColumn(row, filter.column());
            boolean current = actual != null && actual.equals(filter.value());
            matched = and ? matched && current : matched || current;
        }
        return matched;
    }

    private static String resolveColumn(MatterStampData row, String column) {
        if (column == null) {
            return null;
        }
        return switch (column) {
            case "nodeId" -> row.nodeId();
            case "stampType" -> row.stampType();
            case "processId" -> row.processId();
            case "cancelFlag" -> row.cancelFlag();
            default -> null;
        };
    }

    private static List<MatterStampData> paginate(List<MatterStampData> rows, StampListSearchCondition condition) {
        if (condition == null) {
            return rows;
        }
        int offset = parseInt(condition.offset(), 0);
        int count = parseInt(condition.count(), rows.size());
        if (offset >= rows.size()) {
            return List.of();
        }
        int end = Math.min(rows.size(), offset + Math.max(count, 0));
        return rows.subList(offset, end);
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
