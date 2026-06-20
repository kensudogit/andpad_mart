package jp.andpad.imart.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import jp.andpad.imart.auth.config.IntraMartAuthAutoConfiguration;

/**
 * {@link IntraMartAuthorizationService} の結合テスト。
 *
 * <p>スタブモード（{@code app.imart.auth.mode=stub}）でセッション解決、
 * ロール認可、無効セッション拒否を検証する。
 */
@SpringBootTest(classes = IntraMartAuthAutoConfiguration.class)
@TestPropertySource(properties = {
    "app.imart.auth.enabled=true",
    "app.imart.auth.mode=stub",
    "app.imart.auth.dev-session-id=test-session"
})
class IntraMartAuthorizationServiceTest {

    /** テスト対象: 認可ファサード。 */
    @Autowired
    private IntraMartAuthorizationService authorizationService;

    /**
     * 開発用セッション ID から Principal が正しく解決されることを確認する。
     */
    @Test
    void resolvesDevSessionPrincipal() {
        IntraMartSecurityPrincipal principal = authorizationService.resolvePrincipal("test-session");
        assertThat(principal.userCode()).isEqualTo("dev-user");
        assertThat(principal.tenantId()).isEqualTo("local-dev");
        assertThat(principal.roleIds()).contains("andpad-user", "andpad-admin");
    }

    /**
     * 無効なセッション ID で {@link IntraMartAuthException} がスローされることを確認する。
     */
    @Test
    void rejectsInvalidSession() {
        assertThatThrownBy(() -> authorizationService.resolvePrincipal("invalid"))
                .isInstanceOf(IntraMartAuthException.class);
    }

    /**
     * 管理者ロール認可が開発用セッションで成功し、無効セッションで失敗することを確認する。
     */
    @Test
    void certifiesAdminRole() {
        assertThat(authorizationService.hasAdminRole("test-session")).isTrue();
        assertThat(authorizationService.hasRole("invalid", "andpad-admin")).isFalse();
    }
}
