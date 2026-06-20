package jp.andpad.imart.auth.model;

import java.time.Instant;

/**
 * {@code LoginSessionManager.getLoginSessionInfo} の結果を表す。
 */
public record ImLoginSession(
        String sessionId,
        String accountId,
        String userCode,
        String tenantId,
        String displayName,
        String locale,
        Instant loginAt,
        boolean valid) {

    public static ImLoginSession invalid(String sessionId) {
        return new ImLoginSession(sessionId, null, null, null, null, null, null, false);
    }
}
