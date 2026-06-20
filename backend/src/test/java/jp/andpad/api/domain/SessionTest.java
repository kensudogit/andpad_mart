package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.Session;

class SessionTest {

    @Test
    void isRecordType() {
        assertThat(Session.class.isRecord()).isTrue();
    }

}
