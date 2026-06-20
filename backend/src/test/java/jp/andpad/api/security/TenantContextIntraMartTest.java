package jp.andpad.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jp.andpad.imart.auth.IntraMartSecurityPrincipal;

class TenantContextIntraMartTest {

    @Test
    void mapsIntraMartPrincipalToAuthPrincipal() {
        IntraMartSecurityPrincipal im = new IntraMartSecurityPrincipal(
                "acc-1", "user-1", "tenant-1", "sess-1", "Test User", Set.of("andpad-admin"));
        AuthPrincipal mapped = TenantContext.fromIntraMart(im);
        assertThat(mapped.userId()).isEqualTo("user-1");
        assertThat(mapped.orgId()).isEqualTo("tenant-1");
        assertThat(mapped.role()).isEqualTo("admin");
        assertThat(mapped.name()).isEqualTo("Test User");
    }

    @Test
    void mapsMemberRoleWhenNotAdmin() {
        IntraMartSecurityPrincipal im = new IntraMartSecurityPrincipal(
                "acc-1", "user-1", "tenant-1", "sess-1", "Test User", Set.of("andpad-user"));
        AuthPrincipal mapped = TenantContext.fromIntraMart(im);
        assertThat(mapped.role()).isEqualTo("member");
    }
}
