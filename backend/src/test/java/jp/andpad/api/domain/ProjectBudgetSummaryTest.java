package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.ProjectBudgetSummary;

class ProjectBudgetSummaryTest {

    @Test
    void isRecordType() {
        assertThat(ProjectBudgetSummary.class.isRecord()).isTrue();
    }

}
