package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.MemberRole;

class MemberRoleTest {

    @Test
    void valuesAreDefined() {
        assertThat(MemberRole.values()).isNotEmpty();    }

}
