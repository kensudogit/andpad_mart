package jp.andpad.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.security.AuthPrincipal;

class AuthPrincipalTest {

    @Test
    void holdsFields() {
        AuthPrincipal p = new AuthPrincipal("u1", "org1", "OWNER", "a@b.jp", "Name");
        assertThat(p.userId()).isEqualTo("u1");
        assertThat(p.email()).isEqualTo("a@b.jp");
    }

}
