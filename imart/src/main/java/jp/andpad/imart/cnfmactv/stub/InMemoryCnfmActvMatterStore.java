package jp.andpad.imart.cnfmactv.stub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;

@Component
public class InMemoryCnfmActvMatterStore {

    private final ConcurrentMap<String, ActvMatterCnfmData> bySystemMatterId = new ConcurrentHashMap<>();

    public void put(ActvMatterCnfmData data) {
        bySystemMatterId.put(key(data), data);
    }

    public List<ActvMatterCnfmData> listByType(String listType) {
        return bySystemMatterId.values().stream()
                .filter(row -> listType.equals(row.listType()))
                .sorted(Comparator.comparing(ActvMatterCnfmData::arrivedDate, Comparator.nullsLast(String::compareTo))
                        .reversed())
                .toList();
    }

    public List<ActvMatterCnfmData> listByTypeAndFlowIds(String listType, List<String> flowIds) {
        List<ActvMatterCnfmData> rows = listByType(listType);
        if (flowIds == null || flowIds.isEmpty()) {
            return rows;
        }
        return rows.stream().filter(row -> row.flowId() != null && flowIds.contains(row.flowId())).toList();
    }

    private static String key(ActvMatterCnfmData data) {
        return data.listType() + ":" + data.systemMatterId();
    }
}
