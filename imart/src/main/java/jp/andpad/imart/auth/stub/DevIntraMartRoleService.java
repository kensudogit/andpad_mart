package jp.andpad.imart.auth.stub;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.spi.IntraMartRoleService;
import lombok.RequiredArgsConstructor;

/**
 * ローカル開発用 {@code RoleInfoManager} スタブ実装。
 *
 * <p>{@code app.imart.auth.mode=stub} 時に使用。開発用セッションに対して
 * {@code andpad-user} と {@code andpad-admin} の2ロールを内包しているものとして扱う。
 *
 * @see IntraMartRoleService
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartRoleService implements IntraMartRoleService {

    /** 開発用セッションに付与するロール ID 集合。 */
    private static final Set<String> DEV_ROLES = Set.of("andpad-user", "andpad-admin");

    /** 認証設定（開発用セッション ID）。 */
    private final IntraMartAuthProperties properties;

    /** {@inheritDoc} — 開発用セッションかつ {@code DEV_ROLES} に含まれるロールのみ {@code true}。 */
    @Override
    public boolean certify(String sessionId, String roleId) {
        if (!isDevSession(sessionId)) {
            return false;
        }
        return DEV_ROLES.contains(roleId);
    }

    /** {@inheritDoc} — 開発用セッションで {@code roleIds} のいずれかが {@code DEV_ROLES} に含まれるか。 */
    @Override
    public boolean certifyAny(String sessionId, Collection<String> roleIds) {
        if (!isDevSession(sessionId)) {
            return false;
        }
        return roleIds.stream().anyMatch(DEV_ROLES::contains);
    }

    /** {@inheritDoc} — 開発用セッションの場合 {@code DEV_ROLES} を返却。 */
    @Override
    public Collection<String> listRoleIds(String sessionId) {
        if (!isDevSession(sessionId)) {
            return List.of();
        }
        return DEV_ROLES;
    }

    /**
     * 指定セッション ID が開発用セッションか判定する。
     *
     * @param sessionId セッション ID
     * @return 開発用セッションなら {@code true}
     */
    private boolean isDevSession(String sessionId) {
        return properties.getDevSessionId().equals(sessionId);
    }
}
