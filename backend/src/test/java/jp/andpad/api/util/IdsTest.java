package jp.andpad.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.util.Ids;

class IdsTest {

    @Test
    void randomIdHasPrefix() {
        assertThat(Ids.random("test-")).startsWith("test-");
    }

}
