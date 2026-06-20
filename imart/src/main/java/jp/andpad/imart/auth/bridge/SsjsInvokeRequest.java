package jp.andpad.imart.auth.bridge;

import java.util.List;
import java.util.Map;

/**
 * intra-mart SSJS API ブリッジ呼び出しリクエスト DTO。
 *
 * <p>Spring Boot から IM コンテナ内の SSJS スクリプトへ HTTP POST する際の
 * リクエストボディ。IM 側スクリプトが {@code module} / {@code api} / {@code method}
 * を解釈し、対応する SSJS API（{@code RoleInfoManager} 等）を実行する。
 *
 * @param module    API カテゴリ（例: {@code platform}、{@code tenant}、{@code im_workflow}）
 * @param api       API クラス名（例: {@code LoginSessionManager}、{@code WorkflowAuthUtil}）
 * @param method    呼び出すメソッド名
 * @param args      メソッド引数リスト
 * @param sessionId IM セッション ID（IM 側でログインユーザコンテキスト復元に使用）
 * @param context   追加コンテキスト（テナント ID 等、必要に応じて設定）
 */
public record SsjsInvokeRequest(
        String module,
        String api,
        String method,
        List<Object> args,
        String sessionId,
        Map<String, String> context) {}
