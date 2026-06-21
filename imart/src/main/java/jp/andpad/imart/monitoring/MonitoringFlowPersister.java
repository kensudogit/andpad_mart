package jp.andpad.imart.monitoring;

import org.springframework.stereotype.Component;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.spi.MonitoringFlowRecorder;
import lombok.RequiredArgsConstructor;

/** フロー別モニタリング情報を永続化 SPI へ保存する。 */
@Component
@RequiredArgsConstructor
public class MonitoringFlowPersister {

    private final IntraMartMonitoringProperties properties;
    private final MonitoringFlowRecorder monitoringFlowRecorder;

    public void persist(MonitoringFlowData data) {
        if (properties.isStoreInDatabase()) {
            monitoringFlowRecorder.record(data);
        }
    }
}
