package jp.andpad.imart.auth.http;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import jp.andpad.imart.auth.model.WorkflowAuthRequest;
import jp.andpad.imart.auth.spi.IntraMartWorkflowAuthService;
import lombok.RequiredArgsConstructor;

/**
 * HTTP ブリッジ経由の {@code WorkflowAuthUtil} 実装。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartWorkflowAuthService implements IntraMartWorkflowAuthService {

    private final IntraMartSsjsBridgeClient bridgeClient;

    @Override
    public boolean isAuthApply(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthApply",
                List.of(request.flowId(), request.applyBaseDate(), request.authUserCode()),
                request.sessionId());
    }

    @Override
    public boolean isAuthProcess(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthProcess",
                List.of(request.systemMatterId(), request.nodeId()),
                request.sessionId());
    }

    @Override
    public boolean isAuthConfirm(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthConfirm",
                List.of(request.systemMatterId(), request.nodeId()),
                request.sessionId());
    }

    @Override
    public boolean isAuthRefDetail(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthRefDetail",
                List.of(request.systemMatterId(), request.admorType()),
                request.sessionId());
    }

    @Override
    public boolean canApply(WorkflowAuthRequest request) {
        return invokeBoolean("canApply", List.of(buildApplicationKey(request)), request.sessionId());
    }

    @Override
    public boolean canProcess(WorkflowAuthRequest request) {
        return invokeBoolean(
                "canProcess",
                List.of(Map.of(
                        "systemMatterId", request.systemMatterId(),
                        "nodeId", request.nodeId())),
                request.sessionId());
    }

    private boolean invokeBoolean(String method, List<Object> args, String sessionId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "im_workflow", "WorkflowAuthUtil", method, args, sessionId, Map.of()));
        return response.success() && Boolean.TRUE.equals(response.result());
    }

    private static Map<String, String> buildApplicationKey(WorkflowAuthRequest request) {
        return Map.of(
                "flowId", nullToEmpty(request.flowId()),
                "applyBaseDate", nullToEmpty(request.applyBaseDate()),
                "authUserCode", nullToEmpty(request.authUserCode()));
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
