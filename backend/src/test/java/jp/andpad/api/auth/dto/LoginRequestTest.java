package jp.andpad.api.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.auth.dto.LoginRequest;

class LoginRequestTest {

    @Test
    void createsRecord() {
        var value = new LoginRequest("demo@sakura-dental.jp", "demo1234");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
