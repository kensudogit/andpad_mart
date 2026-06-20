package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateCrmContactInput;

class CreateCrmContactInputTest {

    @Test
    void createsRecord() {
        var value = new CreateCrmContactInput("Co", "contact@x.jp", "090", "Company", "LEAD", "NOTE");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
