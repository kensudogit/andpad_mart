package jp.andpad.api.repository;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.AttendanceRecord;
import jp.andpad.api.domain.Contract;
import jp.andpad.api.domain.ContractTemplate;
import jp.andpad.api.domain.CrmContact;
import jp.andpad.api.domain.CrmInteraction;
import jp.andpad.api.domain.DxInitiative;
import jp.andpad.api.domain.LeaveRequest;
import jp.andpad.api.domain.SaasModule;
import jp.andpad.api.domain.SaasModuleCode;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SaasRepository {

    private final JdbcTemplate jdbc;

    public List<SaasModule> listOrgModules(String orgId) {
        return jdbc.query(
                """
                SELECT m.code, m.name, m.description, COALESCE(om.enabled, FALSE) AS enabled
                FROM saas_modules m
                LEFT JOIN org_modules om ON om.module_code = m.code AND om.org_id = ?
                ORDER BY m.code
                """,
                (rs, rowNum) -> new SaasModule(
                        SaasModuleCode.valueOf(rs.getString("code")),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("enabled")),
                orgId);
    }

    public SaasModule setModuleEnabled(String orgId, SaasModuleCode code, boolean enabled) {
        jdbc.update(
                """
                INSERT INTO org_modules (org_id, module_code, enabled)
                VALUES (?, ?, ?)
                ON CONFLICT (org_id, module_code) DO UPDATE SET enabled = EXCLUDED.enabled
                """,
                orgId,
                code.name(),
                enabled);
        return jdbc.queryForObject(
                """
                SELECT m.code, m.name, m.description, om.enabled
                FROM saas_modules m
                JOIN org_modules om ON om.module_code = m.code AND om.org_id = ?
                WHERE m.code = ?
                """,
                (rs, rowNum) -> new SaasModule(
                        SaasModuleCode.valueOf(rs.getString("code")),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("enabled")),
                orgId,
                code.name());
    }

    public List<DxInitiative> listDxInitiatives(String orgId) {
        return jdbc.query(
                """
                SELECT i.id, i.title, i.description, i.status, i.progress_pct, i.owner_name,
                       i.due_date, i.created_at,
                       COUNT(t.id)::int AS task_count,
                       COUNT(t.id) FILTER (WHERE t.done)::int AS tasks_done
                FROM dx_initiatives i
                LEFT JOIN dx_tasks t ON t.initiative_id = i.id
                WHERE i.org_id = ?
                GROUP BY i.id
                ORDER BY i.created_at DESC
                """,
                (rs, rowNum) -> new DxInitiative(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getInt("progress_pct"),
                        rs.getString("owner_name"),
                        formatDate(rs.getDate("due_date")),
                        rs.getInt("task_count"),
                        rs.getInt("tasks_done"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId);
    }

    @Transactional
    public DxInitiative createDxInitiative(String orgId, String title, String description, String status,
            int progressPct, String ownerName, LocalDate dueDate) {
        String id = Ids.random("dxi_");
        String st = status == null || status.isBlank() ? "PLANNED" : status;
        jdbc.update(
                """
                INSERT INTO dx_initiatives (id, org_id, title, description, status, progress_pct, owner_name, due_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                title,
                description == null ? "" : description,
                st,
                progressPct,
                ownerName == null ? "" : ownerName,
                dueDate == null ? null : Date.valueOf(dueDate));
        return new DxInitiative(
                id, title, description == null ? "" : description, st, progressPct,
                ownerName == null ? "" : ownerName, formatDate(dueDate), 0, 0,
                Dates.format(Dates.now()));
    }

    public List<CrmContact> listCrmContacts(String orgId) {
        return jdbc.query(
                """
                SELECT id, name, email, phone, company, stage, notes, created_at
                FROM crm_contacts WHERE org_id = ? ORDER BY created_at DESC
                """,
                (rs, rowNum) -> new CrmContact(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("company"),
                        rs.getString("stage"),
                        rs.getString("notes"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId);
    }

    @Transactional
    public CrmContact createCrmContact(String orgId, String name, String email, String phone, String company,
            String stage, String notes) {
        String id = Ids.random("crm_");
        String st = stage == null || stage.isBlank() ? "LEAD" : stage;
        jdbc.update(
                """
                INSERT INTO crm_contacts (id, org_id, name, email, phone, company, stage, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                name,
                email == null ? "" : email,
                phone == null ? "" : phone,
                company == null ? "" : company,
                st,
                notes == null ? "" : notes);
        return new CrmContact(
                id, name, email == null ? "" : email, phone == null ? "" : phone,
                company == null ? "" : company, st, notes == null ? "" : notes,
                Dates.format(Dates.now()));
    }

    public List<CrmInteraction> listCrmInteractions(String orgId, String contactId) {
        return jdbc.query(
                """
                SELECT id, contact_id, kind, summary, occurred_at
                FROM crm_interactions WHERE org_id = ? AND contact_id = ?
                ORDER BY occurred_at DESC
                """,
                (rs, rowNum) -> new CrmInteraction(
                        rs.getString("id"),
                        rs.getString("contact_id"),
                        rs.getString("kind"),
                        rs.getString("summary"),
                        formatTimestamp(rs.getTimestamp("occurred_at"))),
                orgId,
                contactId);
    }

    @Transactional
    public CrmInteraction createCrmInteraction(String orgId, String contactId, String kind, String summary) {
        String id = Ids.random("cri_");
        String k = kind == null || kind.isBlank() ? "NOTE" : kind;
        jdbc.update(
                "INSERT INTO crm_interactions (id, org_id, contact_id, kind, summary) VALUES (?, ?, ?, ?, ?)",
                id,
                orgId,
                contactId,
                k,
                summary);
        return new CrmInteraction(id, contactId, k, summary, Dates.format(Dates.now()));
    }

    public List<AttendanceRecord> listAttendanceRecords(String orgId) {
        return jdbc.query(
                """
                SELECT a.id, a.user_id, COALESCE(u.name, '') AS user_name, a.clock_in, a.clock_out, a.note
                FROM attendance_records a
                LEFT JOIN users u ON u.id = a.user_id
                WHERE a.org_id = ?
                ORDER BY a.clock_in DESC
                LIMIT 30
                """,
                (rs, rowNum) -> new AttendanceRecord(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getString("user_name"),
                        formatTimestamp(rs.getTimestamp("clock_in")),
                        rs.getTimestamp("clock_out") == null
                                ? null
                                : formatTimestamp(rs.getTimestamp("clock_out")),
                        rs.getString("note")),
                orgId);
    }

    @Transactional
    public AttendanceRecord clockIn(String orgId, String userId, String note) {
        String id = Ids.random("att_");
        jdbc.update(
                "INSERT INTO attendance_records (id, org_id, user_id, clock_in, note) VALUES (?, ?, ?, NOW(), ?)",
                id,
                orgId,
                userId,
                note == null ? "" : note);
        return findAttendance(orgId, id);
    }

    @Transactional
    public AttendanceRecord clockOut(String orgId, String userId) {
        String recordId = jdbc.queryForObject(
                """
                SELECT id FROM attendance_records
                WHERE org_id = ? AND user_id = ? AND clock_out IS NULL
                ORDER BY clock_in DESC LIMIT 1
                """,
                String.class,
                orgId,
                userId);
        jdbc.update(
                "UPDATE attendance_records SET clock_out = NOW() WHERE org_id = ? AND id = ? AND clock_out IS NULL",
                orgId,
                recordId);
        return findAttendance(orgId, recordId);
    }

    public List<LeaveRequest> listLeaveRequests(String orgId) {
        return jdbc.query(
                """
                SELECT l.id, l.user_id, COALESCE(u.name, '') AS user_name, l.start_date, l.end_date,
                       l.reason, l.status, l.created_at
                FROM leave_requests l
                LEFT JOIN users u ON u.id = l.user_id
                WHERE l.org_id = ?
                ORDER BY l.created_at DESC
                """,
                (rs, rowNum) -> new LeaveRequest(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getString("user_name"),
                        formatDate(rs.getDate("start_date")),
                        formatDate(rs.getDate("end_date")),
                        rs.getString("reason"),
                        rs.getString("status"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId);
    }

    @Transactional
    public LeaveRequest createLeaveRequest(String orgId, String userId, LocalDate start, LocalDate end, String reason) {
        String id = Ids.random("lv_");
        jdbc.update(
                """
                INSERT INTO leave_requests (id, org_id, user_id, start_date, end_date, reason)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                userId,
                Date.valueOf(start),
                Date.valueOf(end),
                reason == null ? "" : reason);
        String userName = jdbc.queryForObject("SELECT name FROM users WHERE id = ?", String.class, userId);
        return new LeaveRequest(
                id, userId, userName, start.toString(), end.toString(),
                reason == null ? "" : reason, "PENDING", Dates.format(Dates.now()));
    }

    @Transactional
    public LeaveRequest approveLeaveRequest(String orgId, String id) {
        jdbc.update("UPDATE leave_requests SET status = 'APPROVED' WHERE org_id = ? AND id = ?", orgId, id);
        return findLeaveRequest(orgId, id);
    }

    public List<ContractTemplate> listContractTemplates(String orgId) {
        return jdbc.query(
                "SELECT id, name, body, created_at FROM contract_templates WHERE org_id = ? ORDER BY created_at DESC",
                (rs, rowNum) -> new ContractTemplate(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("body"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId);
    }

    @Transactional
    public ContractTemplate createContractTemplate(String orgId, String name, String body) {
        String id = Ids.random("ctpl_");
        jdbc.update(
                "INSERT INTO contract_templates (id, org_id, name, body) VALUES (?, ?, ?, ?)",
                id,
                orgId,
                name,
                body);
        return new ContractTemplate(id, name, body, Dates.format(Dates.now()));
    }

    public List<Contract> listContracts(String orgId) {
        return jdbc.query(
                """
                SELECT id, COALESCE(template_id, '') AS template_id, title, party_name, party_email,
                       body, status, created_at, signed_at
                FROM contracts WHERE org_id = ? ORDER BY created_at DESC
                """,
                (rs, rowNum) -> new Contract(
                        rs.getString("id"),
                        emptyToNull(rs.getString("template_id")),
                        rs.getString("title"),
                        rs.getString("party_name"),
                        rs.getString("party_email"),
                        rs.getString("body"),
                        rs.getString("status"),
                        formatTimestamp(rs.getTimestamp("created_at")),
                        rs.getTimestamp("signed_at") == null
                                ? null
                                : formatTimestamp(rs.getTimestamp("signed_at"))),
                orgId);
    }

    @Transactional
    public Contract createContract(String orgId, String templateId, String title, String partyName,
            String partyEmail, String body) {
        String id = Ids.random("ctr_");
        String resolvedBody = body;
        if ((resolvedBody == null || resolvedBody.isBlank()) && templateId != null && !templateId.isBlank()) {
            try {
                resolvedBody = jdbc.queryForObject(
                        "SELECT body FROM contract_templates WHERE org_id = ? AND id = ?",
                        String.class,
                        orgId,
                        templateId);
            } catch (EmptyResultDataAccessException ignored) {
                resolvedBody = "";
            }
        }
        if (resolvedBody == null) {
            resolvedBody = "";
        }
        jdbc.update(
                """
                INSERT INTO contracts (id, org_id, template_id, title, party_name, party_email, body, status)
                VALUES (?, ?, NULLIF(?, ''), ?, ?, ?, ?, 'DRAFT')
                """,
                id,
                orgId,
                templateId == null ? "" : templateId,
                title,
                partyName,
                partyEmail == null ? "" : partyEmail,
                resolvedBody);
        return new Contract(
                id, emptyToNull(templateId), title, partyName,
                partyEmail == null ? "" : partyEmail, resolvedBody, "DRAFT",
                Dates.format(Dates.now()), null);
    }

    @Transactional
    public Contract signContract(String orgId, String id) {
        jdbc.update(
                "UPDATE contracts SET status = 'SIGNED', signed_at = NOW() WHERE org_id = ? AND id = ?",
                orgId,
                id);
        return jdbc.queryForObject(
                """
                SELECT id, COALESCE(template_id, '') AS template_id, title, party_name, party_email,
                       body, status, created_at, signed_at
                FROM contracts WHERE org_id = ? AND id = ?
                """,
                (rs, rowNum) -> new Contract(
                        rs.getString("id"),
                        emptyToNull(rs.getString("template_id")),
                        rs.getString("title"),
                        rs.getString("party_name"),
                        rs.getString("party_email"),
                        rs.getString("body"),
                        rs.getString("status"),
                        formatTimestamp(rs.getTimestamp("created_at")),
                        formatTimestamp(rs.getTimestamp("signed_at"))),
                orgId,
                id);
    }

    private AttendanceRecord findAttendance(String orgId, String id) {
        return jdbc.queryForObject(
                """
                SELECT a.id, a.user_id, COALESCE(u.name, '') AS user_name, a.clock_in, a.clock_out, a.note
                FROM attendance_records a
                LEFT JOIN users u ON u.id = a.user_id
                WHERE a.org_id = ? AND a.id = ?
                """,
                (rs, rowNum) -> new AttendanceRecord(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getString("user_name"),
                        formatTimestamp(rs.getTimestamp("clock_in")),
                        rs.getTimestamp("clock_out") == null
                                ? null
                                : formatTimestamp(rs.getTimestamp("clock_out")),
                        rs.getString("note")),
                orgId,
                id);
    }

    private LeaveRequest findLeaveRequest(String orgId, String id) {
        return jdbc.queryForObject(
                """
                SELECT l.id, l.user_id, COALESCE(u.name, '') AS user_name, l.start_date, l.end_date,
                       l.reason, l.status, l.created_at
                FROM leave_requests l
                LEFT JOIN users u ON u.id = l.user_id
                WHERE l.org_id = ? AND l.id = ?
                """,
                (rs, rowNum) -> new LeaveRequest(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getString("user_name"),
                        formatDate(rs.getDate("start_date")),
                        formatDate(rs.getDate("end_date")),
                        rs.getString("reason"),
                        rs.getString("status"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId,
                id);
    }

    private static String formatTimestamp(java.sql.Timestamp ts) {
        return ts == null ? null : Dates.format(ts.toInstant());
    }

    private static String formatDate(Date date) {
        return date == null ? null : date.toLocalDate().toString();
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
