package jp.andpad.imart.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * intra-mart 認証・認可の設定プロパティ。
 *
 * <p>{@code application.yml} または {@code intramart.properties} の
 * {@code app.imart.auth.*} プレフィックスで設定する。
 *
 * <p>対応する intra-mart SSJS API:
 * <ul>
 *   <li>{@code LoginSessionManager} — セッション管理</li>
 *   <li>{@code RoleInfoManager} — ロール認可</li>
 *   <li>{@code WorkflowAuthUtil} — ワークフロー権限</li>
 * </ul>
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/platform/LoginSessionManager/index.html">LoginSessionManager</a>
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/tenant/RoleInfoManager/index.html">RoleInfoManager</a>
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/WorkflowAuthUtil/index.html">WorkflowAuthUtil</a>
 */
@Data
@ConfigurationProperties(prefix = "app.imart.auth")
public class IntraMartAuthProperties {

    /** IM セッション認証を有効化する（デフォルト: {@code false}）。 */
    private boolean enabled = false;

    /**
     * 認証モード。
     * <ul>
     *   <li>{@code stub} — ローカル開発用スタブ（デフォルト）。IM サーバー不要</li>
     *   <li>{@code http} — SSJS ブリッジ HTTP 経由で IM ランタイム API を呼び出し</li>
     * </ul>
     */
    private String mode = "stub";

    /**
     * JWT 認証との併用設定。
     *
     * <p>{@code true} の場合、IM セッション認証を JWT より優先する。
     * IM セッションが無効な場合のみ JWT 認証が適用される。
     */
    private boolean hybridWithJwt = true;

    /** IM 標準セッション Cookie 名（リクエストからセッション ID を取得する際に使用）。 */
    private String sessionCookie = "imart-session";

    /** セッション ID ヘッダー名（Cookie より優先して参照）。 */
    private String sessionHeader = "X-IM-Session-Id";

    /** 開発用セッション ID（{@code stub} モードで有効とみなす ID）。 */
    private String devSessionId = "dev-imart-session";

    /** 開発用アカウント ID（{@code stub} モードのセッション解決時に返却）。 */
    private String devAccountId = "dev-account";

    /** 開発用ユーザコード（{@code stub} モードのセッション解決時に返却）。 */
    private String devUserCode = "dev-user";

    /** 開発用テナント ID（{@code stub} モードのセッション解決時に返却）。 */
    private String devTenantId = "local-dev";

    /** ANDPAD 一般ユーザ向けロール ID（{@code RoleInfoManager.certify} 判定用）。 */
    private String defaultRoleId = "andpad-user";

    /** ANDPAD 管理者ロール ID（{@code RoleInfoManager.certify} 判定用）。 */
    private String adminRoleId = "andpad-admin";

    /** HTTP ブリッジ接続設定（{@code mode=http} 時に使用）。 */
    private final Bridge bridge = new Bridge();

    /**
     * intra-mart SSJS API ブリッジの HTTP 接続設定。
     *
     * <p>Spring Boot から IM コンテナ内の SSJS スクリプトを HTTP 経由で呼び出す際の
     * ベース URL・タイムアウト等を定義する。
     */
    @Data
    public static class Bridge {

        /** intra-mart ベース URL（例: {@code https://im.example.com}）。 */
        private String baseUrl = "";

        /** SSJS API 呼び出しエンドポイントパス。 */
        private String invokePath = "/imart/api/andpad/ssjs-invoke";

        /** HTTP 接続タイムアウト（秒）。 */
        private int connectTimeoutSeconds = 5;

        /** HTTP 読み取りタイムアウト（秒）。 */
        private int readTimeoutSeconds = 30;
    }
}
