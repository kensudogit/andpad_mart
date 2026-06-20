package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.CrmInteraction;

class CrmInteractionTest {

    @Test
    void createsRecord() {
        var value = new CrmInteraction("i1", "c1", "CALL", "summary", "2024-01-01");
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
