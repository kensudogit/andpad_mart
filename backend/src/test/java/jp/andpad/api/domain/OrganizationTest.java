package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.Organization;

class OrganizationTest {

    @Test
    void isRecordType() {
        assertThat(Organization.class.isRecord()).isTrue();
    }

}
