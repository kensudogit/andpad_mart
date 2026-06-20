package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.TeamMember;

class TeamMemberTest {

    @Test
    void isRecordType() {
        assertThat(TeamMember.class.isRecord()).isTrue();
    }

}
