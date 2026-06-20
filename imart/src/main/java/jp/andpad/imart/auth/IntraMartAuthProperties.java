package jp.andpad.imart.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * intra-mart 認証・認可設定。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/platform/LoginSessionManager/index.html">LoginSessionManager</a>
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/tenant/RoleInfoManager/index.html">RoleInfoManager</a>
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/WorkflowAuthUtil/index.html">WorkflowAuthUtil</a>
 */
@Data
@ConfigurationProperties(prefix = "app.imart.auth")
public class IntraMartAuthProperties {

    /** IM セッション認証を有効化する。 */
    private boolean enabled = false;

    /**
     * 認証モード。
     * <ul>
     *   <li>{@code stub} — ローカル開発用スタブ（デフォルト）</li>
     *   <li>{@code http} — SSJS ブリッジ HTTP 経由で IM ランタイム API を呼び出し</li>
     * </ul>
     */
    private String mode = "stub";

    /** JWT と併用する場合、IM セッションを優先する。 */
    private boolean hybridWithJwt = true;

    /** IM 標準セッション Cookie 名。 */
    private String sessionCookie = "imart-session";

    /** セッション ID ヘッダー（Cookie より優先）。 */
    private String sessionHeader = "X-IM-Session-Id";

    /** 開発用セッション ID（stub モード）。 */
    private String devSessionId = "dev-imart-session";

    /** 開発用アカウント ID（stub モード）。 */
    private String devAccountId = "dev-account";

    /** 開発用ユーザコード（stub モード）。 */
    private String devUserCode = "dev-user";

    /** 開発用テナント ID（stub モード）。 */
    private String devTenantId = "local-dev";

    /** ANDPAD 連携用デフォルトロール ID（RoleInfoManager.certify 用）。 */
    private String defaultRoleId = "andpad-user";

    /** 管理者ロール ID。 */
    private String adminRoleId = "andpad-admin";

    private final Bridge bridge = new Bridge();

    @Data
    public static class Bridge {
        /** intra-mart ベース URL（例: https://im.example.com）。 */
        private String baseUrl = "";

        /** SSJS API 呼び出しエンドポイントパス。 */
        private String invokePath = "/imart/api/andpad/ssjs-invoke";

        /** HTTP 接続タイムアウト（秒）。 */
        private int connectTimeoutSeconds = 5;

        /** HTTP 読み取りタイムアウト（秒）。 */
        private int readTimeoutSeconds = 30;
    }
}
