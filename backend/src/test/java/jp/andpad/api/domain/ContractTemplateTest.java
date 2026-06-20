package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.ContractTemplate;

class ContractTemplateTest {

    @Test
    void createsRecord() {
        var value = new ContractTemplate("t1", "Name", "body", "2024-01-01");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
