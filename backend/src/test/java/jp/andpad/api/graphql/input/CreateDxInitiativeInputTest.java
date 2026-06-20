package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateDxInitiativeInput;

class CreateDxInitiativeInputTest {

    @Test
    void createsRecord() {
        var value = new CreateDxInitiativeInput("DX", "desc", "PLANNED", 0, "Owner", "2024-12-31");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
