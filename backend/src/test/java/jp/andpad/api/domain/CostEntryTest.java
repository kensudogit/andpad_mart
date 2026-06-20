package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.CostEntry;

class CostEntryTest {

    @Test
    void isRecordType() {
        assertThat(CostEntry.class.isRecord()).isTrue();
    }

}
