package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateContractInput;

class CreateContractInputTest {

    @Test
    void createsRecord() {
        var value = new CreateContractInput("tmpl-1", "Title", "Party", "party@x.jp", "body");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
