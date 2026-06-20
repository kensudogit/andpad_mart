package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.BudgetType;

class BudgetTypeTest {

    @Test
    void valuesAreDefined() {
        assertThat(BudgetType.values()).isNotEmpty();    }

}
