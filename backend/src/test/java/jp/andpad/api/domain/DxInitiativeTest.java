package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.DxInitiative;

class DxInitiativeTest {

    @Test
    void isRecordType() {
        assertThat(DxInitiative.class.isRecord()).isTrue();
    }

}
