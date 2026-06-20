package jp.andpad.imart.spi;

/**
 * intra-mart セッション・テナント情報への抽象アクセス SPI。
 *
 * <p>intra-mart Accel Platform 上では {@code Contexts.getAccountContext()} や
 * {@code Contexts.getUserContext()} から取得できるログインユーザ・テナント情報を、
 * Spring Boot スタンドアロン環境でも同一インタフェースで参照できるようにする。
 *
 * <p>実装の切り替え:
 * <ul>
 *   <li>本番 IM コンテナ — IM セッション連携実装（{@link jp.andpad.imart.auth.ImAuthIntraMartContext}）</li>
 *   <li>ローカル開発 — {@link jp.andpad.imart.stub.DevIntraMartContext} スタブ</li>
 * </ul>
 *
 * @see jp.andpad.imart.auth.ImAuthIntraMartContext
 * @see jp.andpad.imart.stub.DevIntraMartContext
 */
public interface IntraMartContextSpi {

    /**
     * ログイン中ユーザーのユーザコード（未ログイン時は {@code null}）。
     *
     * @return intra-mart ユーザコード、または未ログイン時 {@code null}
     */
    String getLoginUserId();

    /**
     * テナント（会社・組織）識別子。
     *
     * @return テナント ID（例: {@code local-dev}、本番では IM テナントコード）
     */
    String getTenantId();

    /**
     * intra-mart セッションが有効かどうか。
     *
     * @return ログイン済みでセッションが有効なら {@code true}
     */
    boolean isLoggedIn();

    /**
     * ANDPAD プラグイン ID。
     *
     * <p>intra-mart 上のプラグイン定義（{@code META-INF/intra-mart/andpad-plugin.xml}）
     * と一致させる固定値。
     *
     * @return プラグイン ID（デフォルト: {@code andpad-api}）
     */
    default String getPluginId() {
        return "andpad-api";
    }
}
