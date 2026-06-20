package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.ProjectBudget;

class ProjectBudgetTest {

    @Test
    void isRecordType() {
        assertThat(ProjectBudget.class.isRecord()).isTrue();
    }

}
