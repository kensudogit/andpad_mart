package jp.andpad.imart.auth;

import java.util.Set;

/**
 * intra-mart セッションから解決した認証主体。
 * Spring Security の {@code Authentication#getPrincipal()} に設定される。
 */
public record IntraMartSecurityPrincipal(
        String accountId,
        String userCode,
        String tenantId,
        String sessionId,
        String displayName,
        Set<String> roleIds) {}
