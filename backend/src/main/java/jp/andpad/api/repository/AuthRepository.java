package jp.andpad.api.repository;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.MemberRole;
import jp.andpad.api.domain.Organization;
import jp.andpad.api.domain.PlanTier;
import jp.andpad.api.domain.SubscriptionStatus;
import jp.andpad.api.domain.User;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuthRepository {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public record LoginResult(User user, Organization organization, MemberRole role) {}

    public record RegisterInput(String clinicName, String slug, String ownerName, String email, String password) {}

    public Optional<User> findUserByEmail(String email) {
        String normalized = email.toLowerCase(Locale.ROOT).trim();
        try {
            return Optional.of(jdbc.queryForObject(
                    """
                    SELECT id, email, name, COALESCE(avatar_url, '') AS avatar_url
                    FROM users WHERE LOWER(email) = ?
                    """,
                    (rs, rowNum) -> new User(
                            rs.getString("id"),
                            rs.getString("email"),
                            rs.getString("name"),
                            emptyToNull(rs.getString("avatar_url"))),
                    normalized));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<LoginResult> login(String email, String password) {
        String normalized = email.toLowerCase(Locale.ROOT).trim();
        try {
            var row = jdbc.queryForMap(
                    """
                    SELECT id, email, name, COALESCE(avatar_url, '') AS avatar_url, password_hash
                    FROM users WHERE LOWER(email) = ?
                    """,
                    normalized);
            if (!passwordEncoder.matches(password, (String) row.get("password_hash"))) {
                return Optional.empty();
            }
            User user = new User(
                    (String) row.get("id"),
                    (String) row.get("email"),
                    (String) row.get("name"),
                    emptyToNull((String) row.get("avatar_url")));
            var orgRow = jdbc.queryForMap(
                    """
                    SELECT o.id, o.name, o.slug, o.plan_tier, o.subscription_status, o.seat_count,
                           o.timezone, o.created_at, tm.role
                    FROM team_members tm
                    JOIN organizations o ON o.id = tm.org_id
                    WHERE tm.user_id = ?
                    ORDER BY tm.joined_at ASC
                    LIMIT 1
                    """,
                    user.id());
            jdbc.update(
                    "UPDATE team_members SET last_active_at = NOW() WHERE user_id = ? AND org_id = ?",
                    user.id(),
                    orgRow.get("id"));
            Organization org = mapOrganization(orgRow, 0);
            MemberRole role = MemberRole.valueOf((String) orgRow.get("role"));
            return Optional.of(new LoginResult(user, org, role));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Transactional
    public LoginResult register(RegisterInput input) {
        String email = input.email().toLowerCase(Locale.ROOT).trim();
        String slug = input.slug() == null || input.slug().isBlank()
                ? "clinic-" + System.currentTimeMillis()
                : input.slug().toLowerCase(Locale.ROOT).trim();
        String hash = passwordEncoder.encode(input.password());
        String orgId = Ids.random("org_");
        String userId = Ids.random("user_");
        String tmId = Ids.random("tm_");
        Instant now = Dates.now();
        jdbc.update(
                """
                INSERT INTO organizations (id, name, slug, plan_tier, subscription_status, seat_count)
                VALUES (?, ?, ?, 'STARTER', 'TRIALING', 5)
                """,
                orgId,
                input.clinicName(),
                slug);
        jdbc.update(
                "INSERT INTO users (id, email, name, password_hash) VALUES (?, ?, ?, ?)",
                userId,
                email,
                input.ownerName(),
                hash);
        jdbc.update(
                "INSERT INTO team_members (id, org_id, user_id, role) VALUES (?, ?, ?, 'OWNER')",
                tmId,
                orgId,
                userId);
        jdbc.update("INSERT INTO usage_counters (org_id) VALUES (?)", orgId);
        jdbc.update(
                """
                INSERT INTO org_modules (org_id, module_code, enabled)
                SELECT ?, code, TRUE FROM saas_modules
                """,
                orgId);
        User user = new User(userId, email, input.ownerName(), null);
        Organization org = new Organization(
                orgId,
                input.clinicName(),
                slug,
                PlanTier.STARTER,
                SubscriptionStatus.TRIALING,
                5,
                "Asia/Tokyo",
                1,
                Dates.format(now),
                java.util.List.of());
        return new LoginResult(user, org, MemberRole.OWNER);
    }

    public Optional<LoginResult> sessionByUser(String userId, String orgId) {
        try {
            User user = jdbc.queryForObject(
                    """
                    SELECT id, email, name, COALESCE(avatar_url, '') AS avatar_url
                    FROM users WHERE id = ?
                    """,
                    (rs, rowNum) -> new User(
                            rs.getString("id"),
                            rs.getString("email"),
                            rs.getString("name"),
                            emptyToNull(rs.getString("avatar_url"))),
                    userId);
            var orgRow = jdbc.queryForMap(
                    """
                    SELECT o.id, o.name, o.slug, o.plan_tier, o.subscription_status, o.seat_count,
                           o.timezone, o.created_at, tm.role
                    FROM organizations o
                    JOIN team_members tm ON tm.org_id = o.id
                    WHERE o.id = ? AND tm.user_id = ?
                    """,
                    orgId,
                    userId);
            MemberRole role = MemberRole.valueOf((String) orgRow.get("role"));
            int memberCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM team_members WHERE org_id = ?", Integer.class, orgId);
            Organization org = mapOrganization(orgRow, memberCount);
            return Optional.of(new LoginResult(user, org, role));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private static Organization mapOrganization(java.util.Map<String, Object> row, int memberCount) {
        Instant createdAt = row.get("created_at") instanceof java.sql.Timestamp ts
                ? ts.toInstant()
                : Dates.now();
        return new Organization(
                (String) row.get("id"),
                (String) row.get("name"),
                (String) row.get("slug"),
                PlanTier.valueOf((String) row.get("plan_tier")),
                SubscriptionStatus.valueOf((String) row.get("subscription_status")),
                ((Number) row.get("seat_count")).intValue(),
                (String) row.get("timezone"),
                memberCount,
                Dates.format(createdAt),
                java.util.List.of());
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
