package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.CostEntryType;

class CostEntryTypeTest {

    @Test
    void valuesAreDefined() {
        assertThat(CostEntryType.values()).isNotEmpty();    }

}
