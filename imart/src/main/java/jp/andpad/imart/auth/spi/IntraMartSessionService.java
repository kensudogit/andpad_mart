package jp.andpad.imart.auth.spi;

import java.util.Optional;

import jp.andpad.imart.auth.model.ImLoginSession;

/**
 * intra-mart Platform {@code LoginSessionManager} / {@code Contexts} 相当の SPI。
 */
public interface IntraMartSessionService {

    /** セッション ID からログインセッション情報を取得する。 */
    Optional<ImLoginSession> getLoginSession(String sessionId);

    /** セッションを無効化する（ログアウト）。 */
    void invalidateSession(String sessionId);

    /** 現在のセッションが有効か。 */
    default boolean isSessionValid(String sessionId) {
        return getLoginSession(sessionId).map(ImLoginSession::valid).orElse(false);
    }
}
