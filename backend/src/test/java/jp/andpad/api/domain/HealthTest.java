package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.Health;

class HealthTest {

    @Test
    void createsRecord() {
        var value = new Health(true, "andpad-api", "1.0");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
