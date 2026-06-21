package jp.andpad.imart.stamp.stub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.model.StampFrameNode;

/** ローカル開発用の印影データインメモリストア。 */
@Component
public class InMemoryMatterStampStore {

    private final Map<String, List<MatterStampData>> stampsByMatter = new ConcurrentHashMap<>();
    private final Map<String, List<StampFrameNode>> framesByMatter = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    public void addStamp(MatterStampData stamp) {
        if (stamp.systemMatterId() == null || stamp.systemMatterId().isBlank()) {
            return;
        }
        stampsByMatter.compute(stamp.systemMatterId(), (key, existing) -> {
            List<MatterStampData> rows = existing != null ? new ArrayList<>(existing) : new ArrayList<>();
            rows.add(stamp);
            return List.copyOf(rows);
        });
    }

    public void setFrames(String systemMatterId, List<StampFrameNode> frames) {
        if (systemMatterId == null || systemMatterId.isBlank()) {
            return;
        }
        framesByMatter.put(systemMatterId, List.copyOf(frames));
    }

    public List<MatterStampData> listStamps(String systemMatterId) {
        return stampsByMatter.getOrDefault(systemMatterId, List.of());
    }

    public int countStamps(String systemMatterId) {
        return listStamps(systemMatterId).size();
    }

    public List<StampFrameNode> listFrames(String systemMatterId) {
        return framesByMatter.getOrDefault(systemMatterId, List.of());
    }

    public String nextNo() {
        return String.valueOf(sequence.getAndIncrement());
    }
}
