package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.SubscriptionStatus;

class SubscriptionStatusTest {

    @Test
    void valuesAreDefined() {
        assertThat(SubscriptionStatus.values()).isNotEmpty();    }

}
