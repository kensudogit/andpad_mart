package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.User;

class UserTest {

    @Test
    void createsRecord() {
        var value = new User("u1", "a@b.jp", "Name", null);
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
