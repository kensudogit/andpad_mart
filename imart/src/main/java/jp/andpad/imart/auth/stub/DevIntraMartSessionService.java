package jp.andpad.imart.auth.stub;

import java.time.Instant;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.model.ImLoginSession;
import jp.andpad.imart.auth.spi.IntraMartSessionService;
import lombok.RequiredArgsConstructor;

/**
 * ローカル開発用 {@code LoginSessionManager} スタブ実装。
 *
 * <p>{@code app.imart.auth.mode=stub} 時に使用。設定された {@code devSessionId} のみ
 * 有効セッションとして扱い、それ以外は無効セッションを返す。
 *
 * <p>IM サーバーが無い環境での認証フロー検証・単体テストに使用する。
 *
 * @see IntraMartSessionService
 * @see IntraMartAuthProperties#getDevSessionId()
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartSessionService implements IntraMartSessionService {

    /** 認証設定（開発用セッション ID・アカウント情報等）。 */
    private final IntraMartAuthProperties properties;

    /**
     * {@inheritDoc}
     *
     * <p>スタブ動作: {@code devSessionId} と一致する場合のみ有効セッションを返却。
     */
    @Override
    public Optional<ImLoginSession> getLoginSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        if (!properties.getDevSessionId().equals(sessionId)) {
            return Optional.of(ImLoginSession.invalid(sessionId));
        }
        return Optional.of(new ImLoginSession(
                sessionId,
                properties.getDevAccountId(),
                properties.getDevUserCode(),
                properties.getDevTenantId(),
                "Dev User",
                "ja_JP",
                Instant.now(),
                true));
    }

    /**
     * {@inheritDoc}
     *
     * <p>スタブ動作: 何もしない（no-op）。
     */
    @Override
    public void invalidateSession(String sessionId) {
        // stub モードではセッション状態を保持しないため no-op
    }
}
