package jp.andpad.api.service;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/** /status 向けの DB 接続診断（秘密情報は含めない）。 */
@Service
@RequiredArgsConstructor
public class RuntimeStatusService {

    private final DataSource dataSource;

    public boolean isPostgresConnected() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("SELECT 1");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Map<String, Object> setupStatus(boolean postgresConnected) {
        Map<String, Object> out = new LinkedHashMap<>();
        String dbSource = resolveDatabaseSource();
        out.put("postgres", postgresConnected);
        out.put("databaseSource", StringUtils.hasText(dbSource) ? dbSource : "none");
        out.put("databaseUrl", envPresence("DATABASE_URL"));
        out.put("databasePrivateUrl", envPresence("DATABASE_PRIVATE_URL"));
        out.put("pgHost", envPresence("PGHOST"));
        out.put("jwtSecret", StringUtils.hasText(System.getenv("JWT_SECRET")) ? "set" : "empty");
        out.put("openaiApiKey", envPresence("OPENAI_API_KEY"));
        out.put("railway", isRailway());
        String publicDomain = System.getenv("RAILWAY_PUBLIC_DOMAIN");
        if (StringUtils.hasText(publicDomain) && !publicDomain.contains("${{")) {
            out.put("publicUrl", "https://" + publicDomain.trim());
        }

        String jwtWarning = jwtSecretWarning(System.getenv("JWT_SECRET"));
        if (StringUtils.hasText(jwtWarning)) {
            out.put("jwtSecretWarning", jwtWarning);
        }
        if (!postgresConnected && isRailway()) {
            out.put(
                    "hint",
                    "andpad_j service → Variables → Reference → Postgres → DATABASE_URL. "
                            + "JWT_SECRET = random string (not API key). Redeploy.");
        }
        if (postgresConnected && !"set".equals(envPresence("OPENAI_API_KEY"))) {
            out.put("openaiHint", "AI チャットボット / AI Board 用に OPENAI_API_KEY を Variables に追加して Redeploy してください。");
        }
        return out;
    }

    private static String resolveDatabaseSource() {
        for (String key :
                new String[] {"DATABASE_URL", "DATABASE_PRIVATE_URL", "POSTGRES_URL", "POSTGRES_PRIVATE_URL"}) {
            String value = System.getenv(key);
            if (StringUtils.hasText(value) && !value.contains("${{")) {
                return key;
            }
        }
        String pgHost = System.getenv("PGHOST");
        if (StringUtils.hasText(pgHost) && !pgHost.contains("${{")) {
            return "PGHOST";
        }
        return "";
    }

    private static boolean isRailway() {
        return StringUtils.hasText(System.getenv("RAILWAY_ENVIRONMENT"))
                || StringUtils.hasText(System.getenv("RAILWAY_PROJECT_ID"))
                || StringUtils.hasText(System.getenv("RAILWAY_SERVICE_ID"));
    }

    private static String envPresence(String key) {
        String raw = System.getenv(key);
        if (!StringUtils.hasText(raw)) {
            return "empty";
        }
        if (raw.contains("${{")) {
            return "unresolved";
        }
        return "set";
    }

    private static String jwtSecretWarning(String jwt) {
        if (!StringUtils.hasText(jwt)) {
            return "JWT_SECRET is empty";
        }
        if (jwt.startsWith("sk-ant") || jwt.startsWith("sk-proj") || jwt.startsWith("sk-")) {
            return "JWT_SECRET looks like an API key. Use a random string here; put OpenAI keys in OPENAI_API_KEY.";
        }
        if ("dev-only-change-in-production-min-32-chars".equals(jwt)) {
            return "JWT_SECRET is still the dev default. Set a long random string on Railway.";
        }
        return "";
    }
}
