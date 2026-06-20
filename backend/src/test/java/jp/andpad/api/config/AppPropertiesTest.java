package jp.andpad.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.config.AppProperties;

class AppPropertiesTest {

    @Test
    void createsRecord() {
        var value = new AppProperties(new AppProperties.Jwt("secret-min-32-chars-long-enough", 72));
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
