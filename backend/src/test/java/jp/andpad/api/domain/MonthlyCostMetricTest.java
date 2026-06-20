package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.MonthlyCostMetric;

class MonthlyCostMetricTest {

    @Test
    void createsRecord() {
        var value = new MonthlyCostMetric("2024-01", 1000.0);
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
