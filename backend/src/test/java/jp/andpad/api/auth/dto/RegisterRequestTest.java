package jp.andpad.api.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.auth.dto.RegisterRequest;

class RegisterRequestTest {

    @Test
    void createsRecord() {
        var value = new RegisterRequest("Clinic", "slug", "Owner", "a@b.jp", "pass1234");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
