package jp.andpad.imart.auth.bridge;

/**
 * intra-mart SSJS API ブリッジ呼び出しレスポンス。
 */
public record SsjsInvokeResponse(boolean success, Object result, String error) {

    public static SsjsInvokeResponse ok(Object result) {
        return new SsjsInvokeResponse(true, result, null);
    }

    public static SsjsInvokeResponse fail(String error) {
        return new SsjsInvokeResponse(false, null, error);
    }
}
