package jp.andpad.api.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class Dates {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private Dates() {}

    public static String format(Instant instant) {
        return instant == null ? null : ISO.format(instant);
    }

    public static String format(LocalDate date) {
        return date == null ? null : date.toString();
    }

    public static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Instant.parse(value);
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    public static Instant now() {
        return Instant.now().atOffset(ZoneOffset.UTC).toInstant();
    }
}
