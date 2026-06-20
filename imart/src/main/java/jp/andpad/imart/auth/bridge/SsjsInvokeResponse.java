package jp.andpad.imart.auth.bridge;

/**
 * intra-mart SSJS API ブリッジ呼び出しレスポンス DTO。
 *
 * <p>IM コンテナ内 SSJS スクリプトの実行結果を Spring Boot 側で受け取るための
 * 不変データクラス。
 *
 * @param success 呼び出し成功フラグ
 * @param result  API 戻り値（型は API により異なる: Boolean, Map, List 等）
 * @param error   エラーメッセージ（{@code success=false} 時）
 */
public record SsjsInvokeResponse(boolean success, Object result, String error) {

    /**
     * 成功レスポンスを生成する。
     *
     * @param result API 戻り値
     * @return 成功レスポンス
     */
    public static SsjsInvokeResponse ok(Object result) {
        return new SsjsInvokeResponse(true, result, null);
    }

    /**
     * 失敗レスポンスを生成する。
     *
     * @param error エラーメッセージ
     * @return 失敗レスポンス
     */
    public static SsjsInvokeResponse fail(String error) {
        return new SsjsInvokeResponse(false, null, error);
    }
}
