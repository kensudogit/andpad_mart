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
 *
 * <p>{@code app.imart.auth.mode=http} 時に使用。IM コンテナ内 SSJS スクリプトへ
 * {@code RoleInfoManager.certify} および {@code AccountInfoManager.getAccountRoleInfoList} を
 * HTTP 経由で委譲する。
 *
 * @see IntraMartSsjsBridgeClient
 * @see IntraMartRoleService
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartRoleService implements IntraMartRoleService {

    /** SSJS ブリッジ HTTP クライアント。 */
    private final IntraMartSsjsBridgeClient bridgeClient;

    /**
     * {@inheritDoc}
     *
     * <p>IM ブリッジ呼び出し: {@code tenant.RoleInfoManager.certify(roleId)}
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>各 {@code roleId} に対して {@link #certify} を順次呼び出す。
     */
    @Override
    public boolean certifyAny(String sessionId, Collection<String> roleIds) {
        return roleIds.stream().anyMatch(roleId -> certify(sessionId, roleId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>IM ブリッジ呼び出し: {@code tenant.AccountInfoManager.getAccountRoleInfoList()}
     */
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
