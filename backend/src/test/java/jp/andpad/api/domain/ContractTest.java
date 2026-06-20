package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.Contract;

class ContractTest {

    @Test
    void isRecordType() {
        assertThat(Contract.class.isRecord()).isTrue();
    }

}
