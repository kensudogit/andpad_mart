package jp.andpad.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.util.Dates;
import java.time.Instant;
import java.time.LocalDate;

class DatesTest {

    @Test
    void formatsAndParsesDates() {
        Instant instant = Instant.parse("2024-01-01T00:00:00Z");
        assertThat(Dates.format(instant)).isEqualTo("2024-01-01T00:00:00Z");
        assertThat(Dates.parseInstant("2024-01-01T00:00:00Z")).isEqualTo(instant);
        LocalDate date = LocalDate.of(2024, 6, 1);
        assertThat(Dates.format(date)).isEqualTo("2024-06-01");
        assertThat(Dates.parseDate("2024-06-01")).isEqualTo(date);
    }

}
