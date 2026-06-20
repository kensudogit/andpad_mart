package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.BudgetCategorySummary;

class BudgetCategorySummaryTest {

    @Test
    void isRecordType() {
        assertThat(BudgetCategorySummary.class.isRecord()).isTrue();
    }

}
