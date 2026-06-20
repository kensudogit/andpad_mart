package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.UsageSummary;

class UsageSummaryTest {

    @Test
    void createsRecord() {
        var value = new UsageSummary(1, 10, 2, 100, 0, 1000, 0);
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
