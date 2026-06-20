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
 *
 * <p>{@code app.imart.auth.mode=http} 時に使用。IM コンテナ内 SSJS スクリプトへ
 * {@code WorkflowAuthUtil} の各権限判定メソッドを HTTP 経由で委譲する。
 *
 * @see IntraMartSsjsBridgeClient
 * @see IntraMartWorkflowAuthService
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartWorkflowAuthService implements IntraMartWorkflowAuthService {

    /** SSJS ブリッジ HTTP クライアント。 */
    private final IntraMartSsjsBridgeClient bridgeClient;

    /** {@inheritDoc} */
    @Override
    public boolean isAuthApply(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthApply",
                List.of(request.flowId(), request.applyBaseDate(), request.authUserCode()),
                request.sessionId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAuthProcess(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthProcess",
                List.of(request.systemMatterId(), request.nodeId()),
                request.sessionId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAuthConfirm(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthConfirm",
                List.of(request.systemMatterId(), request.nodeId()),
                request.sessionId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAuthRefDetail(WorkflowAuthRequest request) {
        return invokeBoolean(
                "isAuthRefDetail",
                List.of(request.systemMatterId(), request.admorType()),
                request.sessionId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean canApply(WorkflowAuthRequest request) {
        return invokeBoolean("canApply", List.of(buildApplicationKey(request)), request.sessionId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean canProcess(WorkflowAuthRequest request) {
        return invokeBoolean(
                "canProcess",
                List.of(Map.of(
                        "systemMatterId", request.systemMatterId(),
                        "nodeId", request.nodeId())),
                request.sessionId());
    }

    /**
     * WorkflowAuthUtil の Boolean 戻り値メソッドをブリッジ経由で呼び出す。
     *
     * @param method    IM API メソッド名
     * @param args      メソッド引数
     * @param sessionId IM セッション ID
     * @return API 戻り値が {@code true} なら {@code true}
     */
    private boolean invokeBoolean(String method, List<Object> args, String sessionId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "im_workflow", "WorkflowAuthUtil", method, args, sessionId, Map.of()));
        return response.success() && Boolean.TRUE.equals(response.result());
    }

    /**
     * {@code canApply} 用の ApplicationKey 相当マップを構築する。
     *
     * @param request ワークフロー権限リクエスト
     * @return ApplicationKey フィールドマップ
     */
    private static Map<String, String> buildApplicationKey(WorkflowAuthRequest request) {
        return Map.of(
                "flowId", nullToEmpty(request.flowId()),
                "applyBaseDate", nullToEmpty(request.applyBaseDate()),
                "authUserCode", nullToEmpty(request.authUserCode()));
    }

    /**
     * null を空文字に変換する。
     *
     * @param value 入力文字列
     * @return 非 null 文字列
     */
    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
