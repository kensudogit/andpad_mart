package jp.andpad.imart.auth;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.model.ImLoginSession;
import jp.andpad.imart.auth.model.WorkflowAuthRequest;
import jp.andpad.imart.auth.spi.IntraMartRoleService;
import jp.andpad.imart.auth.spi.IntraMartSessionService;
import jp.andpad.imart.auth.spi.IntraMartWorkflowAuthService;
import lombok.RequiredArgsConstructor;

/**
 * intra-mart API を組み合わせた認可ファサード。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class IntraMartAuthorizationService {

    private final IntraMartAuthProperties properties;
    private final IntraMartSessionService sessionService;
    private final IntraMartRoleService roleService;
    private final IntraMartWorkflowAuthService workflowAuthService;

    public IntraMartSecurityPrincipal resolvePrincipal(String sessionId) {
        ImLoginSession session = sessionService
                .getLoginSession(sessionId)
                .filter(ImLoginSession::valid)
                .orElseThrow(() -> new IntraMartAuthException("invalid or expired IM session"));

        Collection<String> roleIds = roleService.listRoleIds(sessionId);
        Set<String> roles = roleIds.stream().collect(Collectors.toSet());

        return new IntraMartSecurityPrincipal(
                session.accountId(),
                session.userCode(),
                session.tenantId(),
                session.sessionId(),
                session.displayName(),
                roles);
    }

    public boolean hasRole(String sessionId, String roleId) {
        return roleService.certify(sessionId, roleId);
    }

    public boolean hasDefaultRole(String sessionId) {
        return roleService.certify(sessionId, properties.getDefaultRoleId());
    }

    public boolean hasAdminRole(String sessionId) {
        return roleService.certify(sessionId, properties.getAdminRoleId());
    }

    public boolean canAccessWorkflowApply(String sessionId, String flowId, String applyBaseDate) {
        return workflowAuthService.isAuthApply(WorkflowAuthRequest.forApply(sessionId, flowId, applyBaseDate));
    }

    public boolean canProcessWorkflow(String sessionId, String systemMatterId, String nodeId) {
        return workflowAuthService.isAuthProcess(WorkflowAuthRequest.forProcess(sessionId, systemMatterId, nodeId));
    }

    public void logout(String sessionId) {
        sessionService.invalidateSession(sessionId);
    }
}
