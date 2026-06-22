package jp.andpad.imart.asyncprocess.stub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;

/** スタブ用インメモリ非同期処理状況ストア。 */
@Component
public class InMemoryAsyncProcessStore {

    private final ConcurrentMap<String, AsyncProcessStatusData> byAcceptId = new ConcurrentHashMap<>();

    public boolean contains(String acceptId) {
        return byAcceptId.containsKey(acceptId);
    }

    public AsyncProcessStatusData get(String acceptId) {
        return byAcceptId.get(acceptId);
    }

    public void put(AsyncProcessStatusData data) {
        byAcceptId.put(data.acceptId(), data);
    }

    public List<AsyncProcessStatusData> listAll() {
        return byAcceptId.values().stream()
                .sorted(Comparator.comparing(AsyncProcessStatusData::procDate, Comparator.nullsLast(String::compareTo))
                        .reversed())
                .toList();
    }

    public List<AsyncProcessStatusData> listByFlowIds(List<String> flowIds) {
        if (flowIds == null || flowIds.isEmpty()) {
            return listAll();
        }
        List<AsyncProcessStatusData> rows = new ArrayList<>();
        for (AsyncProcessStatusData row : listAll()) {
            if (row.flowId() != null && flowIds.contains(row.flowId())) {
                rows.add(row);
            }
        }
        return rows;
    }

    public List<AsyncProcessStatusData> listBySystemMatterIds(List<String> systemMatterIds) {
        if (systemMatterIds == null || systemMatterIds.isEmpty()) {
            return listAll();
        }
        List<AsyncProcessStatusData> rows = new ArrayList<>();
        for (AsyncProcessStatusData row : listAll()) {
            if (row.systemMatterId() != null && systemMatterIds.contains(row.systemMatterId())) {
                rows.add(row);
            }
        }
        return rows;
    }
}
