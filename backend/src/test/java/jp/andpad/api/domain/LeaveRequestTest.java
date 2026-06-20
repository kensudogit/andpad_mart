package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.LeaveRequest;

class LeaveRequestTest {

    @Test
    void isRecordType() {
        assertThat(LeaveRequest.class.isRecord()).isTrue();
    }

}
