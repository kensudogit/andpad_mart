package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateBudgetLineItemInput;

class CreateBudgetLineItemInputTest {

    @Test
    void createsRecord() {
        var value = new CreateBudgetLineItemInput("bud-demo-1", "DIRECT", "直接", "WBS", "desc", 100.0, 100.0, 0.0, 1);
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
