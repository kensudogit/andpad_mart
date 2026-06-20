package jp.andpad.imart.auth.bridge;

import java.util.List;
import java.util.Map;

/**
 * intra-mart SSJS API ブリッジ呼び出しリクエスト。
 * IM コンテナ内のスクリプトが {@code RoleInfoManager} 等を実行する。
 */
public record SsjsInvokeRequest(
        String module,
        String api,
        String method,
        List<Object> args,
        String sessionId,
        Map<String, String> context) {}
