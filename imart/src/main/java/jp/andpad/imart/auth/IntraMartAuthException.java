package jp.andpad.imart.auth;

/**
 * intra-mart 認証・認可エラー。
 */
public class IntraMartAuthException extends RuntimeException {

    public IntraMartAuthException(String message) {
        super(message);
    }
}
