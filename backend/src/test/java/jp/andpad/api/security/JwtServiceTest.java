package jp.andpad.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.security.JwtService;
import jp.andpad.api.security.AuthPrincipal;

class JwtServiceTest extends AbstractIntegrationTest {

    @Autowired
    JwtService jwtService;

    @Test
    void issueAndParseToken() {
        AuthPrincipal principal = new AuthPrincipal("u1", "org1", "OWNER", "a@b.jp", "Name");
        String token = jwtService.issueToken(principal);
        AuthPrincipal parsed = jwtService.parseToken(token);
        assertThat(parsed.userId()).isEqualTo("u1");
        assertThat(parsed.orgId()).isEqualTo("org1");
    }

}
