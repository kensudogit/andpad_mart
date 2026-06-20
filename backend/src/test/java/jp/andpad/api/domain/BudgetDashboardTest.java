package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.BudgetDashboard;

class BudgetDashboardTest {

    @Test
    void isRecordType() {
        assertThat(BudgetDashboard.class.isRecord()).isTrue();
    }

}
