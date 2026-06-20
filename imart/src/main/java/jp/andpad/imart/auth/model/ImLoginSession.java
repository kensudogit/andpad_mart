package jp.andpad.imart.auth.model;

import java.time.Instant;

/**
 * intra-mart {@code LoginSessionManager.getLoginSessionInfo} の結果 DTO。
 *
 * <p>IM ランタイムから取得したセッション情報を Spring Boot 側で扱うための
 * 不変データクラス。{@link #valid} が {@code false} の場合は無効セッションを表す。
 *
 * @param sessionId   セッション ID
 * @param accountId   アカウント ID
 * @param userCode    ユーザコード
 * @param tenantId    テナント ID
 * @param displayName 表示名
 * @param locale      ロケール（例: {@code ja_JP}）
 * @param loginAt     ログイン日時
 * @param valid       セッション有効フラグ
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

    /**
     * 無効セッションを表すインスタンスを生成する。
     *
     * @param sessionId 対象セッション ID
     * @return 全フィールドが null / {@code valid=false} のセッション
     */
    public static ImLoginSession invalid(String sessionId) {
        return new ImLoginSession(sessionId, null, null, null, null, null, null, false);
    }
}
