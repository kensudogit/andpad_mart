package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.SkillLevel;

class SkillLevelTest {

    @Test
    void valuesAreDefined() {
        assertThat(SkillLevel.values()).isNotEmpty();    }

}
