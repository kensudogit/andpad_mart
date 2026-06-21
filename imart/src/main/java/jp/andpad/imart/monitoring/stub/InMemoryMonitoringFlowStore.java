package jp.andpad.imart.monitoring.stub;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;

/** 開発用インメモリ フロー別モニタリングストア。 */
@Component
public class InMemoryMonitoringFlowStore {

    private final ConcurrentMap<String, MonitoringFlowData> byFlowId = new ConcurrentHashMap<>();

    public boolean contains(String flowId) {
        return byFlowId.containsKey(flowId);
    }

    public MonitoringFlowData get(String flowId) {
        return byFlowId.get(flowId);
    }

    public void put(MonitoringFlowData data) {
        byFlowId.put(data.flowId(), data);
    }

    public List<MonitoringFlowData> listAll() {
        return List.copyOf(byFlowId.values());
    }
}
