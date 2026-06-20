package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateConstructionProjectInput;
import jp.andpad.api.domain.ConstructionProjectStatus;

class CreateConstructionProjectInputTest {

    @Test
    void createsRecord() {
        var value = new CreateConstructionProjectInput("P", "Addr", ConstructionProjectStatus.PLANNING, "Mgr", "2024-01-01", "2024-12-31");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
