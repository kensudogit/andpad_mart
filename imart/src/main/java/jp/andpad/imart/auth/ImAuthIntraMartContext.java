package jp.andpad.imart.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jp.andpad.imart.spi.IntraMartContextSpi;

/**
 * IM セッション認証有効時の {@link IntraMartContextSpi} 実装。
 *
 * <p>Spring Security コンテキストに設定された {@link IntraMartSecurityPrincipal} から
 * ログインユーザ・テナント情報を取得する。{@code app.imart.auth.enabled=true} のとき
 * {@link jp.andpad.imart.stub.DevIntraMartContext} の代わりに使用される。
 *
 * @see IntraMartAuthFilter
 * @see IntraMartContextSpi
 */
@Component
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class ImAuthIntraMartContext implements IntraMartContextSpi {

    /** {@inheritDoc} — SecurityContext からユーザコードを取得。未認証時は {@code null}。 */
    @Override
    public String getLoginUserId() {
        return currentPrincipal().map(IntraMartSecurityPrincipal::userCode).orElse(null);
    }

    /** {@inheritDoc} — SecurityContext からテナント ID を取得。未認証時は {@code local-dev}。 */
    @Override
    public String getTenantId() {
        return currentPrincipal().map(IntraMartSecurityPrincipal::tenantId).orElse("local-dev");
    }

    /** {@inheritDoc} — SecurityContext に IM Principal が存在するかで判定。 */
    @Override
    public boolean isLoggedIn() {
        return currentPrincipal().isPresent();
    }

    /**
     * 現在のスレッドの SecurityContext から IM 認証主体を取得する。
     *
     * @return 認証主体（未設定時は空）
     */
    private static java.util.Optional<IntraMartSecurityPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof IntraMartSecurityPrincipal principal) {
            return java.util.Optional.of(principal);
        }
        return java.util.Optional.empty();
    }
}
