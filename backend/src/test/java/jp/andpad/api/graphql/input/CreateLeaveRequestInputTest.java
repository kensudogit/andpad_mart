package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateLeaveRequestInput;

class CreateLeaveRequestInputTest {

    @Test
    void createsRecord() {
        var value = new CreateLeaveRequestInput("2024-01-01", "2024-01-02", "reason");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
