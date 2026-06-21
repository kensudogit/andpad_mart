package jp.andpad.imart.monitoring.spi;

import java.util.List;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;

/**
 * intra-mart ワークフローモニタリング SPI。
 *
 * <p>IM SSJS API 相当:
 * <ul>
 *   <li>{@code MonitoringManager.createMonitoringFlowData}</li>
 *   <li>{@code MonitoringManager.updateMonitoringFlowData}</li>
 *   <li>{@code MonitoringManager.getMonitoringFlowDataList}</li>
 * </ul>
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/MonitoringManager/index.html">MonitoringManager</a>
 */
public interface IntraMartMonitoringService {

    /** フロー別モニタリング情報を新規作成する。 */
    WorkflowMonitoringResult createMonitoringFlowData(String sessionId, List<MonitoringFlowData> models);

    /** フロー別モニタリング情報を更新する。 */
    WorkflowMonitoringResult updateMonitoringFlowData(String sessionId, List<MonitoringFlowData> models);

    /** フロー別モニタリング情報を取得する。 */
    WorkflowMonitoringResult getMonitoringFlowDataList(
            String sessionId, MonitoringFlowDataSearchCondition condition);
}
