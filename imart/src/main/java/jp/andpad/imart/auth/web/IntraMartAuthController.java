package jp.andpad.imart.auth.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.imart.auth.IntraMartAuthException;
import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.IntraMartAuthorizationService;
import jp.andpad.imart.auth.IntraMartSecurityPrincipal;
import jp.andpad.imart.auth.model.WorkflowAuthRequest;
import jp.andpad.imart.auth.spi.IntraMartWorkflowAuthService;
import lombok.RequiredArgsConstructor;

/**
 * intra-mart 認証・認可 REST API コントローラ。
 *
 * <p>フロントエンドや外部連携から IM セッションの検証、ロール認可、
 * ワークフロー権限チェックを HTTP で行うためのエンドポイントを提供する。
 *
 * <p>ベースパス: {@code /auth/imart}
 *
 * @see IntraMartAuthorizationService
 */
@RestController
@RequestMapping("/auth/imart")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class IntraMartAuthController {

    /** 認証設定。 */
    private final IntraMartAuthProperties properties;

    /** 認可ファサード。 */
    private final IntraMartAuthorizationService authorizationService;

    /** ワークフロー権限 SPI。 */
    private final IntraMartWorkflowAuthService workflowAuthService;

    /**
     * 現在の IM セッション情報を返却する。
     *
     * <p>リクエストヘッダー {@code X-IM-Session-Id} にセッション ID を指定する。
     *
     * @param sessionHeader IM セッション ID ヘッダー
     * @return 認証状態・アカウント情報・ロール一覧
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> session(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader) {
        String sessionId = resolveSessionId(sessionHeader);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", "IM session required"));
        }
        try {
            IntraMartSecurityPrincipal principal = authorizationService.resolvePrincipal(sessionId);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("authenticated", true);
            body.put("accountId", principal.accountId());
            body.put("userCode", principal.userCode());
            body.put("tenantId", principal.tenantId());
            body.put("displayName", principal.displayName());
            body.put("roles", principal.roleIds());
            body.put("mode", properties.getMode());
            return ResponseEntity.ok(body);
        } catch (IntraMartAuthException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", ex.getMessage()));
        }
    }

    /**
     * 指定ロールの内包判定を行う（{@code RoleInfoManager.certify} 相当）。
     *
     * @param sessionHeader IM セッション ID ヘッダー
     * @param body          リクエストボディ（{@code roleId} 必須）
     * @return 認可結果（{@code certified: true/false}）
     */
    @PostMapping("/roles/certify")
    public ResponseEntity<Map<String, Object>> certifyRole(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestBody Map<String, String> body) {
        String sessionId = resolveSessionId(sessionHeader);
        String roleId = body.get("roleId");
        if (sessionId == null || roleId == null) {
            return ResponseEntity.badRequest().body(Map.of("certified", false, "message", "sessionId and roleId required"));
        }
        boolean certified = authorizationService.hasRole(sessionId, roleId);
        return ResponseEntity.ok(Map.of("certified", certified, "roleId", roleId));
    }

    /**
     * ワークフロー権限を判定する（{@code WorkflowAuthUtil} 相当）。
     *
     * <p>{@code action} に応じて呼び出す IM API メソッドが変わる:
     * {@code apply}、{@code process}、{@code confirm}、{@code reference}、
     * {@code canApply}、{@code canProcess}
     *
     * @param sessionHeader IM セッション ID ヘッダー
     * @param body          権限判定パラメータ
     * @return 判定結果（{@code allowed: true/false}）
     */
    @PostMapping("/workflow/check")
    public ResponseEntity<Map<String, Object>> checkWorkflow(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestBody WorkflowCheckBody body) {
        String sessionId = resolveSessionId(sessionHeader);
        if (sessionId == null || body.action() == null) {
            return ResponseEntity.badRequest().body(Map.of("allowed", false, "message", "sessionId and action required"));
        }
        WorkflowAuthRequest request = new WorkflowAuthRequest(
                sessionId,
                body.systemMatterId(),
                body.userDataId(),
                body.flowId(),
                body.nodeId(),
                body.applyBaseDate(),
                body.authUserCode(),
                body.admorType());
        boolean allowed = switch (body.action()) {
            case "apply" -> workflowAuthService.isAuthApply(request);
            case "process" -> workflowAuthService.isAuthProcess(request);
            case "confirm" -> workflowAuthService.isAuthConfirm(request);
            case "reference" -> workflowAuthService.isAuthRefDetail(request);
            case "canApply" -> workflowAuthService.canApply(request);
            case "canProcess" -> workflowAuthService.canProcess(request);
            default -> false;
        };
        return ResponseEntity.ok(Map.of("allowed", allowed, "action", body.action()));
    }

    /**
     * IM セッションを無効化する（ログアウト）。
     *
     * @param sessionHeader IM セッション ID ヘッダー
     * @return ログアウト結果
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader) {
        String sessionId = resolveSessionId(sessionHeader);
        if (sessionId != null) {
            authorizationService.logout(sessionId);
        }
        return ResponseEntity.ok(Map.of("loggedOut", true));
    }

    /**
     * セッション ID ヘッダーを解決する。
     *
     * @param sessionHeader リクエストヘッダー値
     * @return トリム済みセッション ID、未指定時は {@code null}
     */
    private String resolveSessionId(String sessionHeader) {
        if (sessionHeader != null && !sessionHeader.isBlank()) {
            return sessionHeader.trim();
        }
        return null;
    }

    /**
     * ワークフロー権限チェック API のリクエストボディ。
     *
     * @param action         判定種別（{@code apply}、{@code process} 等）
     * @param systemMatterId システム案件 ID
     * @param userDataId     ユーザデータ ID
     * @param flowId         フロー ID
     * @param nodeId         ノード ID
     * @param applyBaseDate  申請基準日
     * @param authUserCode   申請権限者コード
     * @param admorType      管理者種別
     */
    public record WorkflowCheckBody(
            String action,
            String systemMatterId,
            String userDataId,
            String flowId,
            String nodeId,
            String applyBaseDate,
            String authUserCode,
            String admorType) {}
}
