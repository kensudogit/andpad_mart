package jp.andpad.imart.stamp.http;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import jp.andpad.imart.stamp.StampSupport;
import jp.andpad.imart.stamp.model.StampListSearchCondition;
import jp.andpad.imart.stamp.model.WorkflowStampResult;
import jp.andpad.imart.stamp.spi.IntraMartStampService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** HTTP ブリッジ経由の {@code CplMatterStampList} 実装。 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.stamp.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartStampService implements IntraMartStampService {

    private final IntraMartSsjsBridgeClient bridgeClient;

    @Override
    public WorkflowStampResult getStampList(
            String sessionId, String systemMatterId, StampListSearchCondition condition) {
        StampListSearchCondition resolved = condition != null ? condition : StampListSearchCondition.empty();
        return invoke("getStampList", systemMatterId, resolved.toMap(), sessionId);
    }

    @Override
    public WorkflowStampResult getStampListCount(
            String sessionId, String systemMatterId, StampListSearchCondition condition) {
        StampListSearchCondition resolved = condition != null ? condition : StampListSearchCondition.empty();
        return invoke("getStampListCount", systemMatterId, resolved.toMap(), sessionId);
    }

    @Override
    public WorkflowStampResult getStampResultFrameList(
            String sessionId, String systemMatterId, boolean sortType) {
        return invoke("getStampResultFrameList", systemMatterId, String.valueOf(sortType), sessionId);
    }

    private WorkflowStampResult invoke(String method, String systemMatterId, Object condArg, String sessionId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "im_workflow", "CplMatterStampList", method, java.util.List.of(systemMatterId, condArg), sessionId, Map.of()));
        if (!response.success()) {
            log.warn("CplMatterStampList.{} failed: {}", method, response.error());
            return WorkflowStampResult.fail(response.error());
        }
        return StampSupport.mapWorkflowResult(response.result());
    }
}
