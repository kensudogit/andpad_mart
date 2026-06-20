package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.ProjectModuleRecord;

class ProjectModuleRecordTest {

    @Test
    void isRecordType() {
        assertThat(ProjectModuleRecord.class.isRecord()).isTrue();
    }

}
