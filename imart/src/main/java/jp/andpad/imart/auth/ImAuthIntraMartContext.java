package jp.andpad.imart.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jp.andpad.imart.spi.IntraMartContextSpi;

/**
 * IM セッション認証有効時の {@link IntraMartContextSpi} 実装。
 */
@Component
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class ImAuthIntraMartContext implements IntraMartContextSpi {

    @Override
    public String getLoginUserId() {
        return currentPrincipal().map(IntraMartSecurityPrincipal::userCode).orElse(null);
    }

    @Override
    public String getTenantId() {
        return currentPrincipal().map(IntraMartSecurityPrincipal::tenantId).orElse("local-dev");
    }

    @Override
    public boolean isLoggedIn() {
        return currentPrincipal().isPresent();
    }

    private static java.util.Optional<IntraMartSecurityPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof IntraMartSecurityPrincipal principal) {
            return java.util.Optional.of(principal);
        }
        return java.util.Optional.empty();
    }
}
