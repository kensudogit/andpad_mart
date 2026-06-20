package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.UpdateOrganizationInput;

class UpdateOrganizationInputTest {

    @Test
    void createsRecord() {
        var value = new UpdateOrganizationInput("New Name", "sample-construction", 10, "Asia/Tokyo");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
