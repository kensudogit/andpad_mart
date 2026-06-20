package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateProjectBudgetInput;
import jp.andpad.api.domain.BudgetStatus;
import jp.andpad.api.domain.BudgetType;

class CreateProjectBudgetInputTest {

    @Test
    void createsRecord() {
        var value = new CreateProjectBudgetInput("prj-demo-1", "Budget", BudgetType.EXECUTION_BUDGET, BudgetStatus.DRAFT, 1, 1000.0, "notes");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
