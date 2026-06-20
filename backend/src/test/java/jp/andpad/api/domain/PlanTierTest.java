package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.PlanTier;

class PlanTierTest {

    @Test
    void valuesAreDefined() {
        assertThat(PlanTier.values()).isNotEmpty();    }

}
