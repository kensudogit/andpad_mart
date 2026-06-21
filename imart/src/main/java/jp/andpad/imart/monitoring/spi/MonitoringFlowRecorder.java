package jp.andpad.imart.monitoring.spi;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;

/** フロー別モニタリング情報の永続化 SPI。 */
public interface MonitoringFlowRecorder {

    void record(MonitoringFlowData data);
}
