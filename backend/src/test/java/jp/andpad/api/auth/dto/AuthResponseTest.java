package jp.andpad.api.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.auth.dto.AuthResponse;

class AuthResponseTest {

    @Test
    void isRecordType() {
        assertThat(AuthResponse.class.isRecord()).isTrue();
    }

}
