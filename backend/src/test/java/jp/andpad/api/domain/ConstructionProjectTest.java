package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.ConstructionProject;

class ConstructionProjectTest {

    @Test
    void isRecordType() {
        assertThat(ConstructionProject.class.isRecord()).isTrue();
    }

}
