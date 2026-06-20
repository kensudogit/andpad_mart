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
 * ローカル開発用 {@code RoleInfoManager} スタブ。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartRoleService implements IntraMartRoleService {

    private static final Set<String> DEV_ROLES = Set.of("andpad-user", "andpad-admin");

    private final IntraMartAuthProperties properties;

    @Override
    public boolean certify(String sessionId, String roleId) {
        if (!isDevSession(sessionId)) {
            return false;
        }
        return DEV_ROLES.contains(roleId);
    }

    @Override
    public boolean certifyAny(String sessionId, Collection<String> roleIds) {
        if (!isDevSession(sessionId)) {
            return false;
        }
        return roleIds.stream().anyMatch(DEV_ROLES::contains);
    }

    @Override
    public Collection<String> listRoleIds(String sessionId) {
        if (!isDevSession(sessionId)) {
            return List.of();
        }
        return DEV_ROLES;
    }

    private boolean isDevSession(String sessionId) {
        return properties.getDevSessionId().equals(sessionId);
    }
}
