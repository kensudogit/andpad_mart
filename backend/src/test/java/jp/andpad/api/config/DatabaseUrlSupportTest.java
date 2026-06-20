package jp.andpad.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DatabaseUrlSupportTest {

    @Test
    void railwayInternalUsesSslDisable() {
        String raw = "postgresql://postgres:secret@postgres.railway.internal:5432/railway";
        String jdbc = DatabaseUrlSupport.normalizeForJdbc(raw);
        assertThat(jdbc).contains("sslmode=disable");
        assertThat(jdbc).startsWith("jdbc:postgresql://");
        assertThat(jdbc).doesNotContain("secret");
    }

    @Test
    void publicRailwayHostUsesSslRequire() {
        String raw = "postgresql://postgres:secret@containers-us-west-123.railway.app:6543/railway";
        String jdbc = DatabaseUrlSupport.normalizeForJdbc(raw);
        assertThat(jdbc).contains("sslmode=require");
    }

    @Test
    void postgresSchemeIsNormalized() {
        String raw = "postgres://postgres:secret@postgres.railway.internal:5432/railway";
        String jdbc = DatabaseUrlSupport.normalizeForJdbc(raw);
        assertThat(jdbc).startsWith("jdbc:postgresql://postgres.railway.internal:5432/railway");
        assertThat(jdbc).contains("sslmode=disable");
    }

    @Test
    void credentialsAreStrippedFromJdbcUrl() {
        String raw = "postgresql://postgres:secret@postgres.railway.internal:5432/railway";
        String jdbc = DatabaseUrlSupport.normalizeForJdbc(raw);
        assertThat(jdbc).isEqualTo(
                "jdbc:postgresql://postgres.railway.internal:5432/railway?sslmode=disable");
        assertThat(jdbc).doesNotContain("secret");
    }
}
