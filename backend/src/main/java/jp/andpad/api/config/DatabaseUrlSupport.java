package jp.andpad.api.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.StringUtils;

/** Railway / ローカル向け PostgreSQL URL の解決と JDBC 正規化。 */
final class DatabaseUrlSupport {

    private static final Pattern USER_INFO =
            Pattern.compile("^(?:jdbc:)?postgres(?:ql)?://([^@]+@)");

    private static final Pattern JDBC_USER_INFO =
            Pattern.compile("^(jdbc:postgresql://)[^@]+@(.+)$");

    private DatabaseUrlSupport() {}

    static String resolveRawUrl(String fallback) {
        for (String key :
                new String[] {"DATABASE_URL", "DATABASE_PRIVATE_URL", "POSTGRES_URL", "POSTGRES_PRIVATE_URL"}) {
            String value = System.getenv(key);
            if (StringUtils.hasText(value) && !value.contains("${{")) {
                return value.trim();
            }
        }
        String fromComponents = fromPgComponents();
        if (fromComponents != null) {
            return fromComponents;
        }
        return fallback;
    }

    /** どの環境変数から DB URL を解決したか（/status 診断用）。 */
    static String resolveDatabaseSource() {
        for (String key :
                new String[] {"DATABASE_URL", "DATABASE_PRIVATE_URL", "POSTGRES_URL", "POSTGRES_PRIVATE_URL"}) {
            String value = System.getenv(key);
            if (StringUtils.hasText(value) && !value.contains("${{")) {
                return key;
            }
        }
        if (fromPgComponents() != null) {
            return "PGHOST";
        }
        return "";
    }

    static boolean isPostgresUrl(String raw) {
        if (!StringUtils.hasText(raw)) {
            return false;
        }
        String lower = raw.trim().toLowerCase();
        return lower.startsWith("postgresql://")
                || lower.startsWith("postgres://")
                || lower.startsWith("jdbc:postgresql://")
                || lower.startsWith("jdbc:postgres://");
    }

    static String normalizeForJdbc(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String url = raw.trim();

        if (url.startsWith("jdbc:postgres://")) {
            url = "jdbc:postgresql://" + url.substring("jdbc:postgres://".length());
        } else if (url.startsWith("postgres://")) {
            url = "postgresql://" + url.substring("postgres://".length());
        }

        if (url.startsWith("postgresql://")) {
            url = "jdbc:" + url;
        }

        if (!url.startsWith("jdbc:postgresql://")) {
            return url;
        }

        url = stripUserInfoFromJdbcUrl(url);
        return appendSslModeIfMissing(url);
    }

    static void applyCredentialsFromUrl(DataSourceProperties props, String rawUrl) {
        if (StringUtils.hasText(System.getenv("DB_USER")) || StringUtils.hasText(System.getenv("DB_PASSWORD"))) {
            return;
        }
        Matcher matcher = USER_INFO.matcher(rawUrl.trim());
        if (!matcher.find()) {
            return;
        }
        String userInfo = matcher.group(1);
        if (!StringUtils.hasText(userInfo)) {
            return;
        }
        userInfo = userInfo.substring(0, userInfo.length() - 1);
        String[] parts = userInfo.split(":", 2);
        if (parts.length > 0 && StringUtils.hasText(parts[0])) {
            props.setUsername(decode(parts[0]));
        }
        if (parts.length > 1) {
            props.setPassword(decode(parts[1]));
        }
    }

    private static String stripUserInfoFromJdbcUrl(String jdbcUrl) {
        Matcher matcher = JDBC_USER_INFO.matcher(jdbcUrl);
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2);
        }
        return jdbcUrl;
    }

    private static String fromPgComponents() {
        String host = firstEnv("PGHOST", "POSTGRES_HOST");
        String user = firstEnv("PGUSER", "POSTGRES_USER");
        String password = firstEnv("PGPASSWORD", "POSTGRES_PASSWORD");
        String dbName = firstEnv("PGDATABASE", "POSTGRES_DB", "POSTGRES_DATABASE");
        String port = firstEnv("PGPORT", "POSTGRES_PORT");
        if (!StringUtils.hasText(host) || !StringUtils.hasText(user)) {
            return null;
        }
        if (!StringUtils.hasText(port)) {
            port = "5432";
        }
        if (!StringUtils.hasText(dbName)) {
            dbName = "railway";
        }
        if (StringUtils.hasText(password)) {
            return "postgresql://" + encode(user) + ":" + encode(password) + "@" + host + ":" + port + "/" + dbName;
        }
        return "postgresql://" + encode(user) + "@" + host + ":" + port + "/" + dbName;
    }

    private static String firstEnv(String... keys) {
        for (String key : keys) {
            String value = System.getenv(key);
            if (StringUtils.hasText(value) && !value.contains("${{")) {
                return value.trim();
            }
        }
        return null;
    }

    private static String appendSslModeIfMissing(String jdbcUrl) {
        if (jdbcUrl.contains("sslmode=")) {
            return jdbcUrl;
        }
        String mode = shouldDisableSsl(jdbcUrl) ? "disable" : "require";
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=" + mode;
    }

    private static boolean shouldDisableSsl(String url) {
        String lower = url.toLowerCase();
        return lower.contains(".railway.internal")
                || lower.contains("localhost")
                || lower.contains("127.0.0.1");
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String encode(String value) {
        return value.replace("@", "%40").replace(":", "%3A");
    }
}
