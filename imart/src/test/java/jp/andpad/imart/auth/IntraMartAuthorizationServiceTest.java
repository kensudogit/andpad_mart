package jp.andpad.imart.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import jp.andpad.imart.auth.config.IntraMartAuthAutoConfiguration;

@SpringBootTest(classes = IntraMartAuthAutoConfiguration.class)
@TestPropertySource(properties = {
    "app.imart.auth.enabled=true",
    "app.imart.auth.mode=stub",
    "app.imart.auth.dev-session-id=test-session"
})
class IntraMartAuthorizationServiceTest {

    @Autowired
    private IntraMartAuthorizationService authorizationService;

    @Test
    void resolvesDevSessionPrincipal() {
        IntraMartSecurityPrincipal principal = authorizationService.resolvePrincipal("test-session");
        assertThat(principal.userCode()).isEqualTo("dev-user");
        assertThat(principal.tenantId()).isEqualTo("local-dev");
        assertThat(principal.roleIds()).contains("andpad-user", "andpad-admin");
    }

    @Test
    void rejectsInvalidSession() {
        assertThatThrownBy(() -> authorizationService.resolvePrincipal("invalid"))
                .isInstanceOf(IntraMartAuthException.class);
    }

    @Test
    void certifiesAdminRole() {
        assertThat(authorizationService.hasAdminRole("test-session")).isTrue();
        assertThat(authorizationService.hasRole("invalid", "andpad-admin")).isFalse();
    }
}
