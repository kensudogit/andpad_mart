package jp.andpad.imart.monitoring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.spi.MonitoringFlowRecorder;

/** DB 永続化未設定時の no-op 実装。 */
@Component
@ConditionalOnMissingBean(MonitoringFlowRecorder.class)
public class NoopMonitoringFlowRecorder implements MonitoringFlowRecorder {

    @Override
    public void record(MonitoringFlowData data) {
        // no-op
    }
}
