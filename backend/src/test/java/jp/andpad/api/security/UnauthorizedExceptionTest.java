package jp.andpad.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.security.UnauthorizedException;

class UnauthorizedExceptionTest {

    @Test
    void carriesMessage() {
        assertThat(new UnauthorizedException("denied").getMessage()).isEqualTo("denied");
    }

}
