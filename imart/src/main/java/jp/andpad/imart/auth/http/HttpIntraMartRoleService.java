package jp.andpad.imart.auth.http;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import jp.andpad.imart.auth.spi.IntraMartRoleService;
import lombok.RequiredArgsConstructor;

/**
 * HTTP ブリッジ経由の {@code RoleInfoManager} 実装。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartRoleService implements IntraMartRoleService {

    private final IntraMartSsjsBridgeClient bridgeClient;

    @Override
    public boolean certify(String sessionId, String roleId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "tenant",
                "RoleInfoManager",
                "certify",
                List.of(roleId),
                sessionId,
                Map.of()));
        return response.success() && Boolean.TRUE.equals(response.result());
    }

    @Override
    public boolean certifyAny(String sessionId, Collection<String> roleIds) {
        return roleIds.stream().anyMatch(roleId -> certify(sessionId, roleId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> listRoleIds(String sessionId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "tenant",
                "AccountInfoManager",
                "getAccountRoleInfoList",
                List.of(),
                sessionId,
                Map.of()));
        if (!response.success() || !(response.result() instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream().map(Object::toString).toList();
    }
}
