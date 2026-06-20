package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateCostEntryInput;
import jp.andpad.api.domain.CostEntryType;

class CreateCostEntryInputTest {

    @Test
    void createsRecord() {
        var value = new CreateCostEntryInput("prj-demo-1", "bli-demo-1", CostEntryType.MATERIAL, "Vendor", "desc", 1000, "2024-01-01", "INV-1", "recorder");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
