package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.CreateProjectModuleRecordInput;
import jp.andpad.api.domain.SaasModuleCode;

class CreateProjectModuleRecordInputTest {

    @Test
    void createsRecord() {
        var value = new CreateProjectModuleRecordInput("prj-demo-1", SaasModuleCode.BILLING, "Title", "OPEN", "detail", 100.0, "Person", "2024-01-01");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
