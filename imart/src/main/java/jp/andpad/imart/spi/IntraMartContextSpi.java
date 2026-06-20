package jp.andpad.imart.spi;

/**
 * intra-mart セッション・テナント情報への抽象アクセス。
 * 本番では IM コンテキスト実装、ローカルでは {@link jp.andpad.imart.stub.DevIntraMartContext} を使用する。
 */
public interface IntraMartContextSpi {

    /** ログイン中ユーザー ID（未ログイン時は null）。 */
    String getLoginUserId();

    /** テナント（会社・組織）識別子。 */
    String getTenantId();

    /** intra-mart セッションが有効か。 */
    boolean isLoggedIn();

    /** プラグイン ID（ANDPAD 固定: andpad-api）。 */
    default String getPluginId() {
        return "andpad-api";
    }
}
