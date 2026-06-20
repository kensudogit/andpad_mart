package jp.andpad.imart.auth;

/**
 * intra-mart 認証・認可処理中に発生する実行時例外。
 *
 * <p>主な発生ケース:
 * <ul>
 *   <li>セッション ID が無効または期限切れ</li>
 *   <li>SSJS ブリッジ呼び出し失敗</li>
 *   <li>必須の認可パラメータ不足</li>
 * </ul>
 */
public class IntraMartAuthException extends RuntimeException {

    /**
     * メッセージ付きで例外を生成する。
     *
     * @param message エラー詳細メッセージ
     */
    public IntraMartAuthException(String message) {
        super(message);
    }
}
