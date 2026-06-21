package jp.andpad.api.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.andpad.api.domain.SentMailMessage;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import jp.andpad.imart.mail.model.SentMailSnapshot;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MailMessageRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public SentMailMessage insert(String orgId, SentMailSnapshot snapshot) {
        String id = Ids.random("mmsg_");
        String recipients = String.join(", ", snapshot.to());
        String cc = String.join(", ", snapshot.cc());
        String parametersJson = toJson(snapshot.parameters());
        jdbc.update(
                """
                INSERT INTO mail_messages (
                    id, org_id, mail_id, locale_id, recipients, cc, subject, body,
                    parameters, entity_type, entity_id, flow_id, sent
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)
                """,
                id,
                orgId,
                snapshot.mailId(),
                snapshot.localeId(),
                recipients,
                cc,
                snapshot.subject(),
                snapshot.body(),
                parametersJson,
                snapshot.entityType(),
                snapshot.entityId(),
                snapshot.flowId(),
                snapshot.sent());
        return findById(orgId, id).orElseThrow();
    }

    public List<SentMailMessage> list(String orgId, String entityType, String entityId, int limit) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, mail_id, locale_id, recipients, cc, subject, body,
                       entity_type, entity_id, flow_id, sent, created_at
                FROM mail_messages
                WHERE org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (entityType != null && !entityType.isBlank()) {
            sql.append(" AND entity_type = ?");
            args.add(entityType);
        }
        if (entityId != null && !entityId.isBlank()) {
            sql.append(" AND entity_id = ?");
            args.add(entityId);
        }
        sql.append(" ORDER BY created_at DESC LIMIT ?");
        args.add(Math.min(Math.max(limit, 1), 100));
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRow(rs), args.toArray());
    }

    public java.util.Optional<SentMailMessage> findById(String orgId, String id) {
        List<SentMailMessage> rows = jdbc.query(
                """
                SELECT id, mail_id, locale_id, recipients, cc, subject, body,
                       entity_type, entity_id, flow_id, sent, created_at
                FROM mail_messages
                WHERE org_id = ? AND id = ?
                """,
                (rs, rowNum) -> mapRow(rs),
                orgId,
                id);
        return rows.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(rows.getFirst());
    }

    private SentMailMessage mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new SentMailMessage(
                rs.getString("id"),
                rs.getString("mail_id"),
                rs.getString("locale_id"),
                rs.getString("recipients"),
                rs.getString("cc"),
                rs.getString("subject"),
                rs.getString("body"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("flow_id"),
                rs.getBoolean("sent"),
                createdAt != null ? Dates.format(createdAt.toInstant()) : null);
    }

    private String toJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map != null ? map : Map.of());
        } catch (Exception ex) {
            return "{}";
        }
    }
}
