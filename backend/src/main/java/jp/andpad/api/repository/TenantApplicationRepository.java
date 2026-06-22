package jp.andpad.api.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.andpad.api.domain.TenantApplication;
import jp.andpad.api.domain.TenantApplicationDocument;
import jp.andpad.api.domain.TenantApplicationStatus;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TenantApplicationRepository {

    private final JdbcTemplate jdbc;

    public TenantApplication create(
            String applicantOrgId,
            String applicantUserId,
            String name,
            String slug,
            String address,
            String contactName,
            String contactEmail,
            String contactPhone,
            String ownerName,
            String ownerEmail,
            String notes) {
        String id = Ids.random("tapp_");
        jdbc.update(
                """
                INSERT INTO tenant_applications (
                    id, applicant_org_id, applicant_user_id, name, slug, address,
                    contact_name, contact_email, contact_phone, owner_name, owner_email, notes, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT')
                """,
                id,
                applicantOrgId,
                applicantUserId,
                name,
                slug,
                address,
                contactName,
                contactEmail,
                contactPhone,
                ownerName,
                ownerEmail,
                notes);
        return findById(id).orElseThrow();
    }

    public Optional<TenantApplication> findById(String id) {
        try {
            var row = jdbc.queryForMap(
                    """
                    SELECT id, applicant_org_id, applicant_user_id, name, slug, address,
                           contact_name, contact_email, contact_phone, owner_name, owner_email, notes,
                           status, workflow_instance_id, created_org_id, submitted_at, approved_at, created_at, updated_at
                    FROM tenant_applications WHERE id = ?
                    """,
                    id);
            return Optional.of(mapApplication(row, listDocuments(id)));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<TenantApplication> listForApplicant(String applicantOrgId, String applicantUserId, boolean platformView) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, applicant_org_id, applicant_user_id, name, slug, address,
                       contact_name, contact_email, contact_phone, owner_name, owner_email, notes,
                       status, workflow_instance_id, created_org_id, submitted_at, approved_at, created_at, updated_at
                FROM tenant_applications
                """);
        List<Object> args = new ArrayList<>();
        if (!platformView) {
            sql.append(" WHERE applicant_org_id = ? AND applicant_user_id = ?");
            args.add(applicantOrgId);
            args.add(applicantUserId);
        }
        sql.append(" ORDER BY created_at DESC");
        return jdbc.query(sql.toString(), args.toArray(), (rs, rowNum) -> mapApplication(rs, listDocuments(rs.getString("id"))));
    }

    public TenantApplication updateDraft(
            String id,
            String name,
            String slug,
            String address,
            String contactName,
            String contactEmail,
            String contactPhone,
            String ownerName,
            String ownerEmail,
            String notes) {
        jdbc.update(
                """
                UPDATE tenant_applications SET
                    name = ?, slug = ?, address = ?, contact_name = ?, contact_email = ?, contact_phone = ?,
                    owner_name = ?, owner_email = ?, notes = ?, updated_at = NOW()
                WHERE id = ? AND status = 'DRAFT'
                """,
                name,
                slug,
                address,
                contactName,
                contactEmail,
                contactPhone,
                ownerName,
                ownerEmail,
                notes,
                id);
        return findById(id).orElseThrow();
    }

    public void markSubmitted(String id, String workflowInstanceId) {
        jdbc.update(
                """
                UPDATE tenant_applications SET
                    status = 'PENDING_APPROVAL', workflow_instance_id = ?, submitted_at = NOW(), updated_at = NOW()
                WHERE id = ?
                """,
                workflowInstanceId,
                id);
    }

    public void markApproved(String id, String createdOrgId) {
        jdbc.update(
                """
                UPDATE tenant_applications SET
                    status = 'ACTIVE', created_org_id = ?, approved_at = NOW(), updated_at = NOW()
                WHERE id = ?
                """,
                createdOrgId,
                id);
    }

    public void markRejected(String id) {
        jdbc.update(
                """
                UPDATE tenant_applications SET status = 'REJECTED', updated_at = NOW()
                WHERE id = ?
                """,
                id);
    }

    public boolean slugTaken(String slug, String excludeId) {
        String normalized = slug.toLowerCase(Locale.ROOT).trim();
        Boolean taken = jdbc.queryForObject(
                """
                SELECT EXISTS(
                    SELECT 1 FROM organizations WHERE LOWER(slug) = ?
                    UNION ALL
                    SELECT 1 FROM tenant_applications
                    WHERE LOWER(slug) = ? AND status NOT IN ('REJECTED') AND (? IS NULL OR id <> ?)
                )
                """,
                Boolean.class,
                normalized,
                normalized,
                excludeId,
                excludeId);
        return Boolean.TRUE.equals(taken);
    }

    public TenantApplicationDocument addDocument(
            String applicationId, String fileName, String contentType, String contentText) {
        String id = Ids.random("tdoc_");
        jdbc.update(
                """
                INSERT INTO tenant_application_documents (id, application_id, file_name, content_type, content_text)
                VALUES (?, ?, ?, ?, ?)
                """,
                id,
                applicationId,
                fileName,
                contentType,
                contentText);
        return new TenantApplicationDocument(id, applicationId, fileName, contentType, Dates.format(Dates.now()));
    }

    public List<TenantApplicationDocument> listDocuments(String applicationId) {
        return jdbc.query(
                """
                SELECT id, application_id, file_name, content_type, uploaded_at
                FROM tenant_application_documents
                WHERE application_id = ?
                ORDER BY uploaded_at ASC
                """,
                (rs, rowNum) -> mapDocument(rs),
                applicationId);
    }

    private static TenantApplication mapApplication(java.util.Map<String, Object> row, List<TenantApplicationDocument> docs) {
        return new TenantApplication(
                (String) row.get("id"),
                (String) row.get("applicant_org_id"),
                (String) row.get("applicant_user_id"),
                (String) row.get("name"),
                (String) row.get("slug"),
                (String) row.get("address"),
                (String) row.get("contact_name"),
                (String) row.get("contact_email"),
                (String) row.get("contact_phone"),
                (String) row.get("owner_name"),
                (String) row.get("owner_email"),
                (String) row.get("notes"),
                TenantApplicationStatus.valueOf((String) row.get("status")),
                (String) row.get("workflow_instance_id"),
                (String) row.get("created_org_id"),
                formatTs(row.get("submitted_at")),
                formatTs(row.get("approved_at")),
                formatTs(row.get("created_at")),
                formatTs(row.get("updated_at")),
                docs);
    }

    private TenantApplication mapApplication(ResultSet rs, List<TenantApplicationDocument> docs) throws SQLException {
        return new TenantApplication(
                rs.getString("id"),
                rs.getString("applicant_org_id"),
                rs.getString("applicant_user_id"),
                rs.getString("name"),
                rs.getString("slug"),
                rs.getString("address"),
                rs.getString("contact_name"),
                rs.getString("contact_email"),
                rs.getString("contact_phone"),
                rs.getString("owner_name"),
                rs.getString("owner_email"),
                rs.getString("notes"),
                TenantApplicationStatus.valueOf(rs.getString("status")),
                rs.getString("workflow_instance_id"),
                rs.getString("created_org_id"),
                formatTs(rs.getTimestamp("submitted_at")),
                formatTs(rs.getTimestamp("approved_at")),
                formatTs(rs.getTimestamp("created_at")),
                formatTs(rs.getTimestamp("updated_at")),
                docs);
    }

    private static TenantApplicationDocument mapDocument(ResultSet rs) throws SQLException {
        return new TenantApplicationDocument(
                rs.getString("id"),
                rs.getString("application_id"),
                rs.getString("file_name"),
                rs.getString("content_type"),
                formatTs(rs.getTimestamp("uploaded_at")));
    }

    private static String formatTs(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp ts) {
            return Dates.format(ts.toInstant());
        }
        return String.valueOf(value);
    }
}
