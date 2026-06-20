package jp.andpad.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.auth.AuthController;

class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void loginReturnsToken() throws Exception {
        String token = loginToken();
        assertThat(token).isNotBlank();
    }

}
