package jp.andpad.imart.auth.spi;

import java.util.Optional;

import jp.andpad.imart.auth.model.ImLoginSession;

/**
 * intra-mart Platform セッション管理 SPI。
 *
 * <p>intra-mart SSJS API の {@code LoginSessionManager} および
 * {@code Contexts.getAccountContext()} / {@code getUserContext()} に相当する操作を抽象化する。
 *
 * <p>実装:
 * <ul>
 *   <li>{@link jp.andpad.imart.auth.stub.DevIntraMartSessionService} — ローカルスタブ</li>
 *   <li>{@link jp.andpad.imart.auth.http.HttpIntraMartSessionService} — HTTP ブリッジ</li>
 * </ul>
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/platform/LoginSessionManager/index.html">LoginSessionManager</a>
 */
public interface IntraMartSessionService {

    /**
     * セッション ID からログインセッション情報を取得する。
     *
     * <p>IM API 相当: {@code LoginSessionManager.getLoginSessionInfo(sessionId)}
     *
     * @param sessionId IM セッション ID
     * @return セッション情報（存在しない場合は空、無効セッションは {@code valid=false}）
     */
    Optional<ImLoginSession> getLoginSession(String sessionId);

    /**
     * セッションを無効化する（ログアウト）。
     *
     * <p>IM API 相当: {@code LoginSessionManager.invalidateLoginSession(sessionId)}
     *
     * @param sessionId 無効化対象のセッション ID
     */
    void invalidateSession(String sessionId);

    /**
     * セッションが有効かどうかを判定する。
     *
     * @param sessionId IM セッション ID
     * @return 有効なら {@code true}
     */
    default boolean isSessionValid(String sessionId) {
        return getLoginSession(sessionId).map(ImLoginSession::valid).orElse(false);
    }
}
