package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.BillingReconciliationItem;

class BillingReconciliationItemTest {

    @Test
    void isRecordType() {
        assertThat(BillingReconciliationItem.class.isRecord()).isTrue();
    }

}
