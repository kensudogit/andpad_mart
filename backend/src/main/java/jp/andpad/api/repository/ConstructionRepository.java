package jp.andpad.api.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.ConstructionProject;
import jp.andpad.api.domain.ConstructionProjectStatus;
import jp.andpad.api.domain.ProjectModuleRecord;
import jp.andpad.api.domain.SaasModuleCode;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConstructionRepository {

    private final JdbcTemplate jdbc;

    public List<ConstructionProject> listProjects(String orgId) {
        return jdbc.query(
                """
                SELECT p.id, p.name, p.site_address, p.status, p.manager_name,
                       p.start_date, p.end_date, p.created_at, COUNT(r.id)::int AS record_count
                FROM construction_projects p
                LEFT JOIN project_module_records r ON r.project_id = p.id
                WHERE p.org_id = ?
                GROUP BY p.id
                ORDER BY p.created_at DESC
                """,
                (rs, rowNum) -> new ConstructionProject(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("site_address"),
                        ConstructionProjectStatus.valueOf(rs.getString("status")),
                        rs.getString("manager_name"),
                        formatDate(rs.getDate("start_date")),
                        formatDate(rs.getDate("end_date")),
                        rs.getInt("record_count"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId);
    }

    @Transactional
    public ConstructionProject createProject(String orgId, String name, String siteAddress,
            ConstructionProjectStatus status, String managerName, LocalDate startDate, LocalDate endDate) {
        String id = Ids.random("prj_");
        ConstructionProjectStatus st = status == null ? ConstructionProjectStatus.PLANNING : status;
        jdbc.update(
                """
                INSERT INTO construction_projects (id, org_id, name, site_address, status, manager_name, start_date, end_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                name,
                siteAddress == null ? "" : siteAddress,
                st.name(),
                managerName == null ? "" : managerName,
                startDate == null ? null : Date.valueOf(startDate),
                endDate == null ? null : Date.valueOf(endDate));
        return new ConstructionProject(
                id, name, siteAddress == null ? "" : siteAddress, st,
                managerName == null ? "" : managerName,
                formatDate(startDate), formatDate(endDate), 0,
                Dates.format(Dates.now()));
    }

    public List<ProjectModuleRecord> listModuleRecords(String orgId, SaasModuleCode code, String projectId) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT r.id, r.project_id, p.name AS project_name, r.module_code, r.title, r.status,
                       r.detail, r.amount, r.person_name, r.record_date, r.created_at
                FROM project_module_records r
                JOIN construction_projects p ON p.id = r.project_id
                WHERE r.org_id = ? AND r.module_code = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        args.add(code.name());
        if (projectId != null && !projectId.isBlank()) {
            sql.append(" AND r.project_id = ?");
            args.add(projectId);
        }
        sql.append(" ORDER BY r.created_at DESC");
        return jdbc.query(
                sql.toString(),
                (rs, rowNum) -> new ProjectModuleRecord(
                        rs.getString("id"),
                        rs.getString("project_id"),
                        rs.getString("project_name"),
                        SaasModuleCode.valueOf(rs.getString("module_code")),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("detail"),
                        rs.getObject("amount") == null ? null : rs.getDouble("amount"),
                        rs.getString("person_name"),
                        formatDate(rs.getDate("record_date")),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                args.toArray());
    }

    @Transactional
    public ProjectModuleRecord createModuleRecord(String orgId, String projectId, SaasModuleCode moduleCode,
            String title, String status, String detail, Double amount, String personName, LocalDate recordDate) {
        String id = Ids.random("rec_");
        String st = status == null || status.isBlank() ? "OPEN" : status;
        String projectName = jdbc.queryForObject(
                "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
                String.class,
                projectId,
                orgId);
        jdbc.update(
                """
                INSERT INTO project_module_records (id, org_id, project_id, module_code, title, status, detail,
                    amount, person_name, record_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                projectId,
                moduleCode.name(),
                title,
                st,
                detail == null ? "" : detail,
                amount,
                personName == null ? "" : personName,
                recordDate == null ? null : Date.valueOf(recordDate));
        return new ProjectModuleRecord(
                id, projectId, projectName, moduleCode, title, st,
                detail == null ? "" : detail, amount,
                personName == null ? "" : personName,
                formatDate(recordDate),
                Dates.format(Dates.now()));
    }

    public String projectName(String orgId, String projectId) {
        return jdbc.queryForObject(
                "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
                String.class,
                projectId,
                orgId);
    }

    private static String formatTimestamp(java.sql.Timestamp ts) {
        return ts == null ? Dates.format(Dates.now()) : Dates.format(ts.toInstant());
    }

    private static String formatDate(Date date) {
        return date == null ? null : date.toLocalDate().toString();
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
