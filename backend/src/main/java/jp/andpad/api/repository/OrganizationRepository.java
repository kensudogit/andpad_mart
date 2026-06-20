package jp.andpad.api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.andpad.api.domain.MemberRole;
import jp.andpad.api.domain.Organization;
import jp.andpad.api.domain.PlanTier;
import jp.andpad.api.domain.SaasModule;
import jp.andpad.api.domain.SubscriptionStatus;
import jp.andpad.api.domain.TeamMember;
import jp.andpad.api.domain.UsageSummary;
import jp.andpad.api.domain.User;
import jp.andpad.api.util.Dates;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrganizationRepository {

    private final JdbcTemplate jdbc;

    public Organization getOrganization(String orgId, List<SaasModule> enabledModules) {
        var row = jdbc.queryForMap(
                """
                SELECT id, name, slug, plan_tier, subscription_status, seat_count, timezone, created_at
                FROM organizations WHERE id = ?
                """,
                orgId);
        int memberCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM team_members WHERE org_id = ?", Integer.class, orgId);
        return mapOrganization(row, memberCount, enabledModules);
    }

    public Organization updateOrganization(String orgId, Map<String, Object> patch, List<SaasModule> enabledModules) {
        Organization current = getOrganization(orgId, enabledModules);
        String name = patch.containsKey("name") ? (String) patch.get("name") : current.name();
        String slug = patch.containsKey("slug") ? (String) patch.get("slug") : current.slug();
        int seatCount = patch.containsKey("seatCount") ? (int) patch.get("seatCount") : current.seatCount();
        String timezone = patch.containsKey("timezone") ? (String) patch.get("timezone") : current.timezone();
        jdbc.update(
                "UPDATE organizations SET name = ?, slug = ?, seat_count = ?, timezone = ? WHERE id = ?",
                name,
                slug,
                seatCount,
                timezone,
                orgId);
        return getOrganization(orgId, enabledModules);
    }

    public List<TeamMember> listTeamMembers(String orgId) {
        return jdbc.query(
                """
                SELECT tm.id, tm.role, tm.joined_at, tm.last_active_at,
                       u.id AS user_id, u.email, u.name, COALESCE(u.avatar_url, '') AS avatar_url
                FROM team_members tm
                JOIN users u ON u.id = tm.user_id
                WHERE tm.org_id = ?
                """,
                (rs, rowNum) -> {
                    User user = new User(
                            rs.getString("user_id"),
                            rs.getString("email"),
                            rs.getString("name"),
                            emptyToNull(rs.getString("avatar_url")));
                    return new TeamMember(
                            rs.getString("id"),
                            user,
                            MemberRole.valueOf(rs.getString("role")),
                            formatTimestamp(rs.getTimestamp("joined_at")),
                            formatTimestamp(rs.getTimestamp("last_active_at")));
                },
                orgId);
    }

    public UsageSummary usageSummary(String orgId) {
        int members = jdbc.queryForObject(
                "SELECT COUNT(*) FROM team_members WHERE org_id = ?", Integer.class, orgId);
        int projects = jdbc.queryForObject(
                "SELECT COUNT(*) FROM construction_projects WHERE org_id = ?", Integer.class, orgId);
        int apiCalls = 0;
        int consultTokens = 0;
        try {
            var row = jdbc.queryForMap(
                    """
                    SELECT COALESCE(api_calls_month, 0) AS api_calls_month,
                           COALESCE(consult_tokens_month, 0) AS consult_tokens_month
                    FROM usage_counters WHERE org_id = ?
                    """,
                    orgId);
            apiCalls = ((Number) row.get("api_calls_month")).intValue();
            consultTokens = ((Number) row.get("consult_tokens_month")).intValue();
        } catch (EmptyResultDataAccessException ignored) {
        }
        return new UsageSummary(members, 10, projects, 50, apiCalls, 10000, consultTokens);
    }

    public boolean orgExists(String orgId) {
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM organizations WHERE id = ?)", Boolean.class, orgId);
        return Boolean.TRUE.equals(exists);
    }

    private static Organization mapOrganization(Map<String, Object> row, int memberCount, List<SaasModule> modules) {
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
                modules);
    }

    private static String formatTimestamp(java.sql.Timestamp ts) {
        return ts == null ? Dates.format(Dates.now()) : Dates.format(ts.toInstant());
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
