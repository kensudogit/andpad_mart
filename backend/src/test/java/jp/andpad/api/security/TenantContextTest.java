package jp.andpad.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.security.TenantContext;
import jp.andpad.api.security.AuthPrincipal;

class TenantContextTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsDemoIdsWhenUnauthenticated() {
        assertThat(TenantContext.orgId()).isEqualTo(TenantContext.DEMO_ORG_ID);
        assertThat(TenantContext.userId()).isEqualTo(TenantContext.DEMO_USER_ID);
    }

    @Test
    void readsAuthenticatedPrincipal() {
        AuthPrincipal principal = new AuthPrincipal("u9", "org9", "OWNER", "a@b.jp", "Name");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));
        assertThat(TenantContext.orgId()).isEqualTo("org9");
        assertThat(TenantContext.userId()).isEqualTo("u9");
    }

}
