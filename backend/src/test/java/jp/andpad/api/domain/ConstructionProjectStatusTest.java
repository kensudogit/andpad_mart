package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.ConstructionProjectStatus;

class ConstructionProjectStatusTest {

    @Test
    void valuesAreDefined() {
        assertThat(ConstructionProjectStatus.values()).isNotEmpty();    }

}
