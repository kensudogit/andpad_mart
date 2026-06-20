package jp.andpad.api.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.ConstructionProjectStatus;
import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsDashboard;
import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsKpi;
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration;
import jp.andpad.api.domain.ExtendedTypes.BimModel;
import jp.andpad.api.domain.ExtendedTypes.ModuleUsageMetric;
import jp.andpad.api.domain.ExtendedTypes.ProjectStatusCount;
import jp.andpad.api.domain.MonthlyCostMetric;
import jp.andpad.api.domain.SaasModuleCode;
import jp.andpad.api.graphql.input.LearningInputs.CreateApiIntegrationInput;
import jp.andpad.api.graphql.input.LearningInputs.CreateBimModelInput;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExtendedRepository {

    private final JdbcTemplate jdbc;

    public AndpadAnalyticsDashboard andpadAnalytics(String orgId, int periodDays) {
        int days = periodDays <= 0 ? 30 : periodDays;
        Instant since = Instant.now().minusSeconds((long) days * 86400L);
        Timestamp sinceTs = Timestamp.from(since);

        int active = queryInt(
                """
                SELECT COUNT(*) FROM construction_projects
                WHERE org_id = ? AND status = 'IN_PROGRESS'
                """,
                orgId);

        double billingTotal = queryDouble(
                """
                SELECT COALESCE(SUM(amount), 0) FROM project_module_records
                WHERE org_id = ? AND module_code = 'BILLING' AND created_at >= ?
                """,
                orgId,
                sinceTs);

        List<ProjectStatusCount> projectsByStatus = jdbc.query(
                """
                SELECT status, COUNT(*)::int FROM construction_projects
                WHERE org_id = ? GROUP BY status
                """,
                (rs, rowNum) -> new ProjectStatusCount(
                        ConstructionProjectStatus.valueOf(rs.getString("status")),
                        rs.getInt("count")),
                orgId);

        List<ModuleUsageMetric> moduleUsage = jdbc.query(
                """
                SELECT r.module_code, COALESCE(m.name, r.module_code) AS module_name, COUNT(*)::int
                FROM project_module_records r
                LEFT JOIN saas_modules m ON m.code = r.module_code
                WHERE r.org_id = ? AND r.created_at >= ?
                GROUP BY r.module_code, m.name
                ORDER BY COUNT(*) DESC
                LIMIT 10
                """,
                (rs, rowNum) -> new ModuleUsageMetric(
                        SaasModuleCode.valueOf(rs.getString("module_code")),
                        rs.getString("module_name"),
                        rs.getInt("count")),
                orgId,
                sinceTs);

        int totalRecords = queryInt(
                """
                SELECT COUNT(*) FROM project_module_records
                WHERE org_id = ? AND created_at >= ?
                """,
                orgId,
                sinceTs);

        int totalProjects = queryInt(
                "SELECT COUNT(*) FROM construction_projects WHERE org_id = ?",
                orgId);

        List<Double> recordsByWeek = new ArrayList<>(4);
        Instant now = Instant.now();
        for (int i = 0; i < 4; i++) {
            Instant weekStart = now.minusSeconds(7L * (4 - i) * 86400L);
            Instant weekEnd = now.minusSeconds(7L * (3 - i) * 86400L);
            recordsByWeek.add((double) queryInt(
                    """
                    SELECT COUNT(*) FROM project_module_records
                    WHERE org_id = ? AND created_at >= ? AND created_at < ?
                    """,
                    orgId,
                    Timestamp.from(weekStart),
                    Timestamp.from(weekEnd)));
        }

        int inProgress = 0;
        int completed = 0;
        int onHold = 0;
        for (ProjectStatusCount s : projectsByStatus) {
            switch (s.status()) {
                case IN_PROGRESS -> inProgress = s.count();
                case COMPLETED -> completed = s.count();
                case ON_HOLD -> onHold = s.count();
                default -> { }
            }
        }

        double projectHealthScore = 0;
        if (totalProjects > 0) {
            projectHealthScore = (double) (inProgress + completed) / totalProjects * 100;
            projectHealthScore -= (double) onHold / totalProjects * 25;
            projectHealthScore = Math.max(0, Math.min(100, projectHealthScore));
        }

        double budgetTotal = queryDouble(
                """
                SELECT COALESCE(SUM(budget_amount), 0) FROM budget_line_items WHERE org_id = ?
                """,
                orgId);
        double actualSum = queryDouble(
                """
                SELECT COALESCE(SUM(actual_amount), 0) FROM budget_line_items WHERE org_id = ?
                """,
                orgId);
        double budgetVariancePct = budgetTotal > 0 ? (budgetTotal - actualSum) / budgetTotal * 100 : 0;

        double costTotal = queryDouble(
                """
                SELECT COALESCE(SUM(amount), 0) FROM cost_entries
                WHERE org_id = ? AND entry_date >= ?
                """,
                orgId,
                Date.valueOf(since.atOffset(java.time.ZoneOffset.UTC).toLocalDate()));

        List<MonthlyCostMetric> costByMonth = orgMonthlyCosts(orgId, 6);
        Double trend = 5.2;

        List<AndpadAnalyticsKpi> kpis = List.of(
                new AndpadAnalyticsKpi("進行中案件", active, "件", trend),
                new AndpadAnalyticsKpi("登録案件", totalProjects, "件", null),
                new AndpadAnalyticsKpi("期間内記録", totalRecords, "件", null),
                new AndpadAnalyticsKpi("請求合計", billingTotal, "円", null),
                new AndpadAnalyticsKpi("実行予算合計", budgetTotal, "円", null),
                new AndpadAnalyticsKpi("期間内原価", costTotal, "円", null),
                new AndpadAnalyticsKpi("予算差異率", budgetVariancePct, "%", null));

        return new AndpadAnalyticsDashboard(
                days,
                kpis,
                projectsByStatus,
                moduleUsage,
                billingTotal,
                active,
                recordsByWeek,
                projectHealthScore,
                budgetTotal,
                costTotal,
                budgetVariancePct,
                costByMonth,
                Dates.format(Dates.now()));
    }

    public List<ApiIntegration> listApiIntegrations(String orgId) {
        return jdbc.query(
                """
                SELECT id, name, provider, endpoint_url, api_key_hint, status, last_sync_at, created_at
                FROM api_integrations WHERE org_id = ? ORDER BY created_at DESC
                """,
                (rs, rowNum) -> mapApiIntegration(rs),
                orgId);
    }

    @Transactional
    public ApiIntegration createApiIntegration(String orgId, CreateApiIntegrationInput input) {
        String id = Ids.random("api_");
        jdbc.update(
                """
                INSERT INTO api_integrations (id, org_id, name, provider, endpoint_url, api_key_hint, status)
                VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
                """,
                id,
                orgId,
                input.name(),
                input.provider() == null ? "" : input.provider(),
                input.endpointUrl() == null ? "" : input.endpointUrl(),
                input.apiKeyHint() == null ? "" : input.apiKeyHint());
        return getApiIntegration(orgId, id);
    }

    @Transactional
    public ApiIntegration syncApiIntegration(String orgId, String id) {
        Timestamp now = Timestamp.from(Dates.now());
        jdbc.update(
                """
                UPDATE api_integrations SET last_sync_at = ?, status = 'ACTIVE'
                WHERE id = ? AND org_id = ?
                """,
                now,
                id,
                orgId);
        return getApiIntegration(orgId, id);
    }

    public List<BimModel> listBimModels(String orgId, String projectId) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT b.id, b.project_id, p.name AS project_name, b.title, b.format, b.viewer_url,
                       b.file_size_mb, b.status, b.uploaded_by, b.created_at
                FROM bim_models b
                JOIN construction_projects p ON p.id = b.project_id
                WHERE b.org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (projectId != null && !projectId.isBlank()) {
            sql.append(" AND b.project_id = ?");
            args.add(projectId);
        }
        sql.append(" ORDER BY b.created_at DESC");
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapBimModel(rs), args.toArray());
    }

    public BimModel getBimModel(String orgId, String id) {
        try {
            return jdbc.queryForObject(
                    """
                    SELECT b.id, b.project_id, p.name AS project_name, b.title, b.format, b.viewer_url,
                           b.file_size_mb, b.status, b.uploaded_by, b.created_at
                    FROM bim_models b
                    JOIN construction_projects p ON p.id = b.project_id
                    WHERE b.id = ? AND b.org_id = ?
                    """,
                    (rs, rowNum) -> mapBimModel(rs),
                    id,
                    orgId);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Transactional
    public BimModel createBimModel(String orgId, CreateBimModelInput input) {
        String id = Ids.random("bim_");
        String format = input.format() == null || input.format().isBlank() ? "IFC" : input.format();
        String viewerUrl = input.viewerUrl() == null || input.viewerUrl().isBlank()
                ? "https://demo.bimdata.io/viewer"
                : input.viewerUrl();
        String projectName = jdbc.queryForObject(
                "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
                String.class,
                input.projectId(),
                orgId);
        jdbc.update(
                """
                INSERT INTO bim_models (id, org_id, project_id, title, format, viewer_url, file_size_mb, status, uploaded_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'READY', ?)
                """,
                id,
                orgId,
                input.projectId(),
                input.title(),
                format,
                viewerUrl,
                input.fileSizeMb(),
                input.uploadedBy() == null ? "" : input.uploadedBy());
        return new BimModel(
                id,
                input.projectId(),
                projectName,
                input.title(),
                format,
                viewerUrl,
                input.fileSizeMb(),
                "READY",
                input.uploadedBy() == null ? "" : input.uploadedBy(),
                Dates.format(Dates.now()));
    }

    private ApiIntegration getApiIntegration(String orgId, String id) {
        return jdbc.queryForObject(
                """
                SELECT id, name, provider, endpoint_url, api_key_hint, status, last_sync_at, created_at
                FROM api_integrations WHERE id = ? AND org_id = ?
                """,
                (rs, rowNum) -> mapApiIntegration(rs),
                id,
                orgId);
    }

    private ApiIntegration mapApiIntegration(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp lastSync = rs.getTimestamp("last_sync_at");
        return new ApiIntegration(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("provider"),
                rs.getString("endpoint_url"),
                rs.getString("api_key_hint"),
                rs.getString("status"),
                lastSync == null ? null : Dates.format(lastSync.toInstant()),
                formatTimestamp(rs.getTimestamp("created_at")));
    }

    private BimModel mapBimModel(java.sql.ResultSet rs) throws java.sql.SQLException {
        Double size = rs.getObject("file_size_mb") == null ? null : rs.getDouble("file_size_mb");
        return new BimModel(
                rs.getString("id"),
                rs.getString("project_id"),
                rs.getString("project_name"),
                rs.getString("title"),
                rs.getString("format"),
                rs.getString("viewer_url"),
                size,
                rs.getString("status"),
                rs.getString("uploaded_by"),
                formatTimestamp(rs.getTimestamp("created_at")));
    }

    private List<MonthlyCostMetric> orgMonthlyCosts(String orgId, int months) {
        LocalDate since = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        Map<String, Double> byMonth = new HashMap<>();
        jdbc.query(
                """
                SELECT to_char(date_trunc('month', entry_date), 'YYYY-MM') AS month,
                       COALESCE(SUM(amount), 0) AS amount
                FROM cost_entries
                WHERE org_id = ? AND entry_date >= ?
                GROUP BY 1 ORDER BY 1
                """,
                rs -> {
                    while (rs.next()) {
                        byMonth.put(rs.getString("month"), rs.getDouble("amount"));
                    }
                },
                orgId,
                Date.valueOf(since));
        List<MonthlyCostMetric> out = new ArrayList<>();
        YearMonth start = YearMonth.from(since);
        for (int i = 0; i < months; i++) {
            String key = start.plusMonths(i).toString();
            out.add(new MonthlyCostMetric(key, byMonth.getOrDefault(key, 0.0)));
        }
        return out;
    }

    private int queryInt(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private double queryDouble(String sql, Object... args) {
        Double value = jdbc.queryForObject(sql, Double.class, args);
        return value == null ? 0.0 : value;
    }

    private static String formatTimestamp(Timestamp ts) {
        return ts == null ? Dates.format(Dates.now()) : Dates.format(ts.toInstant());
    }
}
