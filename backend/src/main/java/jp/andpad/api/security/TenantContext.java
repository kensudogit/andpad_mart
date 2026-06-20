package jp.andpad.api.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jp.andpad.imart.auth.IntraMartSecurityPrincipal;

public final class TenantContext {

    public static final String DEMO_ORG_ID = "org_demo";
    public static final String DEMO_USER_ID = "user_demo";

    private TenantContext() {}

    public static Optional<AuthPrincipal> principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal p) {
            return Optional.of(p);
        }
        if (auth != null && auth.getPrincipal() instanceof IntraMartSecurityPrincipal im) {
            return Optional.of(fromIntraMart(im));
        }
        return Optional.empty();
    }

    public static AuthPrincipal fromIntraMart(IntraMartSecurityPrincipal im) {
        String role = im.roleIds().contains("andpad-admin") ? "admin" : "member";
        return new AuthPrincipal(
                im.userCode(),
                im.tenantId(),
                role,
                im.userCode() + "@imart.local",
                im.displayName() != null ? im.displayName() : im.userCode());
    }

    public static String orgId() {
        return principal().map(AuthPrincipal::orgId).orElse(DEMO_ORG_ID);
    }

    public static String userId() {
        return principal().map(AuthPrincipal::userId).orElse(DEMO_USER_ID);
    }

    public static AuthPrincipal requirePrincipal() {
        return principal().orElseThrow(() -> new UnauthorizedException("authentication required"));
    }
}
