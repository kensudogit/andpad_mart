package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.CrmContact;

class CrmContactTest {

    @Test
    void isRecordType() {
        assertThat(CrmContact.class.isRecord()).isTrue();
    }

}
