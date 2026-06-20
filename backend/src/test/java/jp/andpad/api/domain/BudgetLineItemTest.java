package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.BudgetLineItem;

class BudgetLineItemTest {

    @Test
    void isRecordType() {
        assertThat(BudgetLineItem.class.isRecord()).isTrue();
    }

}
