package jp.andpad.imart.auth.stub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.model.WorkflowAuthRequest;
import jp.andpad.imart.auth.spi.IntraMartWorkflowAuthService;
import lombok.RequiredArgsConstructor;

/**
 * ローカル開発用 {@code WorkflowAuthUtil} スタブ実装。
 *
 * <p>{@code app.imart.auth.mode=stub} 時に使用。開発用セッションに対して
 * すべてのワークフロー権限判定を {@code true} として返す。
 *
 * @see IntraMartWorkflowAuthService
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartWorkflowAuthService implements IntraMartWorkflowAuthService {

    /** 認証設定（開発用セッション ID）。 */
    private final IntraMartAuthProperties properties;

    /** {@inheritDoc} — 開発用セッションなら常に {@code true}。 */
    @Override
    public boolean isAuthApply(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    /** {@inheritDoc} — 開発用セッションなら常に {@code true}。 */
    @Override
    public boolean isAuthProcess(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    /** {@inheritDoc} — 開発用セッションなら常に {@code true}。 */
    @Override
    public boolean isAuthConfirm(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    /** {@inheritDoc} — 開発用セッションなら常に {@code true}。 */
    @Override
    public boolean isAuthRefDetail(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    /** {@inheritDoc} — 開発用セッションなら常に {@code true}。 */
    @Override
    public boolean canApply(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    /** {@inheritDoc} — 開発用セッションなら常に {@code true}。 */
    @Override
    public boolean canProcess(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    /**
     * 指定セッション ID が開発用セッションか判定する。
     *
     * @param sessionId セッション ID
     * @return 開発用セッションなら {@code true}
     */
    private boolean isDevSession(String sessionId) {
        return properties.getDevSessionId().equals(sessionId);
    }
}
