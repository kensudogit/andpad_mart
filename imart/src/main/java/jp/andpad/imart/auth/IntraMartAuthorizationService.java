package jp.andpad.imart.auth;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.model.ImLoginSession;
import jp.andpad.imart.auth.model.WorkflowAuthRequest;
import jp.andpad.imart.auth.spi.IntraMartRoleService;
import jp.andpad.imart.auth.spi.IntraMartSessionService;
import jp.andpad.imart.auth.spi.IntraMartWorkflowAuthService;
import lombok.RequiredArgsConstructor;

/**
 * intra-mart 認証・認可ファサードサービス。
 *
 * <p>セッション管理（{@link IntraMartSessionService}）、ロール認可（{@link IntraMartRoleService}）、
 * ワークフロー権限（{@link IntraMartWorkflowAuthService}）を統合し、
 * フィルターや REST コントローラから単一 API で利用できるようにする。
 *
 * <p>{@code app.imart.auth.enabled=true} のときのみ Bean 登録される。
 *
 * @see IntraMartAuthFilter
 * @see jp.andpad.imart.auth.web.IntraMartAuthController
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class IntraMartAuthorizationService {

    /** 認証・認可設定。 */
    private final IntraMartAuthProperties properties;

    /** IM セッション管理 SPI。 */
    private final IntraMartSessionService sessionService;

    /** IM ロール認可 SPI。 */
    private final IntraMartRoleService roleService;

    /** IM ワークフロー権限 SPI。 */
    private final IntraMartWorkflowAuthService workflowAuthService;

    /**
     * セッション ID から認証主体を解決する。
     *
     * <p>セッションが無効な場合は {@link IntraMartAuthException} をスローする。
     * 有効な場合、アカウント情報とロール一覧を含む {@link IntraMartSecurityPrincipal} を返却する。
     *
     * @param sessionId IM セッション ID
     * @return 解決済み認証主体
     * @throws IntraMartAuthException セッションが無効または期限切れの場合
     */
    public IntraMartSecurityPrincipal resolvePrincipal(String sessionId) {
        ImLoginSession session = sessionService
                .getLoginSession(sessionId)
                .filter(ImLoginSession::valid)
                .orElseThrow(() -> new IntraMartAuthException("invalid or expired IM session"));

        Collection<String> roleIds = roleService.listRoleIds(sessionId);
        Set<String> roles = roleIds.stream().collect(Collectors.toSet());

        return new IntraMartSecurityPrincipal(
                session.accountId(),
                session.userCode(),
                session.tenantId(),
                session.sessionId(),
                session.displayName(),
                roles);
    }

    /**
     * 指定ロールを内包しているか判定する（{@code RoleInfoManager.certify} 相当）。
     *
     * @param sessionId IM セッション ID
     * @param roleId    判定対象ロール ID
     * @return 内包していれば {@code true}
     */
    public boolean hasRole(String sessionId, String roleId) {
        return roleService.certify(sessionId, roleId);
    }

    /**
     * デフォルトロール（{@code andpad-user}）を内包しているか判定する。
     *
     * @param sessionId IM セッション ID
     * @return 内包していれば {@code true}
     */
    public boolean hasDefaultRole(String sessionId) {
        return roleService.certify(sessionId, properties.getDefaultRoleId());
    }

    /**
     * 管理者ロール（{@code andpad-admin}）を内包しているか判定する。
     *
     * @param sessionId IM セッション ID
     * @return 内包していれば {@code true}
     */
    public boolean hasAdminRole(String sessionId) {
        return roleService.certify(sessionId, properties.getAdminRoleId());
    }

    /**
     * ワークフロー申請画面へのアクセス権限を判定する（{@code WorkflowAuthUtil.isAuthApply} 相当）。
     *
     * @param sessionId     IM セッション ID
     * @param flowId        フロー ID
     * @param applyBaseDate 申請基準日（{@code yyyy/MM/dd} 形式）
     * @return 権限があれば {@code true}
     */
    public boolean canAccessWorkflowApply(String sessionId, String flowId, String applyBaseDate) {
        return workflowAuthService.isAuthApply(WorkflowAuthRequest.forApply(sessionId, flowId, applyBaseDate));
    }

    /**
     * ワークフロー処理画面へのアクセス権限を判定する（{@code WorkflowAuthUtil.isAuthProcess} 相当）。
     *
     * @param sessionId      IM セッション ID
     * @param systemMatterId システム案件 ID
     * @param nodeId         ノード ID
     * @return 権限があれば {@code true}
     */
    public boolean canProcessWorkflow(String sessionId, String systemMatterId, String nodeId) {
        return workflowAuthService.isAuthProcess(WorkflowAuthRequest.forProcess(sessionId, systemMatterId, nodeId));
    }

    /**
     * IM セッションを無効化する（ログアウト）。
     *
     * @param sessionId 無効化対象のセッション ID
     */
    public void logout(String sessionId) {
        sessionService.invalidateSession(sessionId);
    }
}
