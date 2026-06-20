package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.AttendanceRecord;

class AttendanceRecordTest {

    @Test
    void isRecordType() {
        assertThat(AttendanceRecord.class.isRecord()).isTrue();
    }

}
