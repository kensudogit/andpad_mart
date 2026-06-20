package jp.andpad.imart.auth;

import java.util.Set;

/**
 * intra-mart セッションから解決した認証主体（Principal）。
 *
 * <p>Spring Security の {@code Authentication#getPrincipal()} に設定され、
 * {@code backend} モジュールの {@link jp.andpad.api.security.TenantContext} が
 * 既存の {@link jp.andpad.api.security.AuthPrincipal} へマッピングする。
 *
 * @param accountId   intra-mart アカウント ID
 * @param userCode    intra-mart ユーザコード
 * @param tenantId    テナント ID
 * @param sessionId   IM セッション ID
 * @param displayName 表示名
 * @param roleIds     内包ロール ID 集合（{@code RoleInfoManager} から取得）
 */
public record IntraMartSecurityPrincipal(
        String accountId,
        String userCode,
        String tenantId,
        String sessionId,
        String displayName,
        Set<String> roleIds) {}
