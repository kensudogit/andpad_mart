package jp.andpad.api.repository;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.ExtendedTypes.ConsultMessage;
import jp.andpad.api.domain.ExtendedTypes.ConsultThread;
import jp.andpad.api.domain.ExtendedTypes.RagDocument;
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit;
import jp.andpad.api.graphql.input.LearningInputs.CreateRagDocumentInput;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import jp.andpad.api.util.RagHelper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConsultRagRepository {

    private final JdbcTemplate jdbc;

    public List<ConsultThread> listThreads(String orgId, String userId, boolean orgWide) {
        String sql = orgWide
                ? """
                SELECT id, title, created_at FROM consultation_threads
                WHERE org_id = ? ORDER BY created_at DESC
                """
                : """
                SELECT id, title, created_at FROM consultation_threads
                WHERE org_id = ? AND user_id = ? ORDER BY created_at DESC
                """;
        if (orgWide) {
            return jdbc.query(
                    sql,
                    (rs, rowNum) -> new ConsultThread(
                            rs.getString("id"),
                            rs.getString("title"),
                            formatTimestamp(rs.getTimestamp("created_at")),
                            List.of()),
                    orgId);
        }
        return jdbc.query(
                sql,
                (rs, rowNum) -> new ConsultThread(
                        rs.getString("id"),
                        rs.getString("title"),
                        formatTimestamp(rs.getTimestamp("created_at")),
                        List.of()),
                orgId,
                userId);
    }

    public ConsultThread getThread(String orgId, String userId, String threadId, boolean orgWide) {
        try {
            ConsultThread thread = orgWide
                    ? jdbc.queryForObject(
                            """
                            SELECT id, title, created_at FROM consultation_threads
                            WHERE org_id = ? AND id = ?
                            """,
                            (rs, rowNum) -> new ConsultThread(
                                    rs.getString("id"),
                                    rs.getString("title"),
                                    formatTimestamp(rs.getTimestamp("created_at")),
                                    List.of()),
                            orgId,
                            threadId)
                    : jdbc.queryForObject(
                            """
                            SELECT id, title, created_at FROM consultation_threads
                            WHERE org_id = ? AND user_id = ? AND id = ?
                            """,
                            (rs, rowNum) -> new ConsultThread(
                                    rs.getString("id"),
                                    rs.getString("title"),
                                    formatTimestamp(rs.getTimestamp("created_at")),
                                    List.of()),
                            orgId,
                            userId,
                            threadId);
            List<ConsultMessage> messages = listMessages(orgId, threadId);
            return new ConsultThread(thread.id(), thread.title(), thread.createdAt(), messages);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<ConsultMessage> listMessages(String orgId, String threadId) {
        return jdbc.query(
                """
                SELECT id, role, content, created_at FROM consultation_messages
                WHERE org_id = ? AND thread_id = ? ORDER BY created_at ASC
                """,
                (rs, rowNum) -> new ConsultMessage(
                        rs.getString("id"),
                        rs.getString("role"),
                        rs.getString("content"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId,
                threadId);
    }

    @Transactional
    public ConsultThread createThread(String orgId, String userId, String title) {
        String id = Ids.random("ct_");
        jdbc.update(
                """
                INSERT INTO consultation_threads (id, org_id, user_id, title) VALUES (?, ?, ?, ?)
                """,
                id,
                orgId,
                userId,
                title);
        return new ConsultThread(id, title, Dates.format(Dates.now()), List.of());
    }

    public boolean verifyThreadAccess(String orgId, String userId, String threadId, boolean orgWide) {
        if (orgWide) {
            Integer count = jdbc.queryForObject(
                    """
                    SELECT COUNT(*) FROM consultation_threads
                    WHERE id = ? AND org_id = ?
                    """,
                    Integer.class,
                    threadId,
                    orgId);
            return count != null && count > 0;
        }
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM consultation_threads
                WHERE id = ? AND org_id = ? AND user_id = ?
                """,
                Integer.class,
                threadId,
                orgId,
                userId);
        return count != null && count > 0;
    }

    @Transactional
    public ConsultMessage addMessage(String orgId, String threadId, String role, String content) {
        String id = Ids.random("cm_");
        Timestamp now = Timestamp.from(Dates.now());
        jdbc.update(
                """
                INSERT INTO consultation_messages (id, org_id, thread_id, role, content, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                threadId,
                role,
                content,
                now);
        return new ConsultMessage(id, role, content, Dates.format(now.toInstant()));
    }

    @Transactional
    public void incrementConsultUsage(String orgId, int tokens) {
        jdbc.update(
                """
                INSERT INTO usage_counters (org_id, consult_tokens_month) VALUES (?, ?)
                ON CONFLICT (org_id) DO UPDATE SET
                    consult_tokens_month = usage_counters.consult_tokens_month + EXCLUDED.consult_tokens_month
                """,
                orgId,
                tokens);
    }

    public List<RagDocument> listRagDocuments(String orgId) {
        return jdbc.query(
                """
                SELECT id, title, content, tags, created_at FROM rag_documents
                WHERE org_id = ? ORDER BY created_at DESC
                """,
                (rs, rowNum) -> new RagDocument(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        readTags(rs),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId);
    }

    @Transactional
    public RagDocument createRagDocument(String orgId, CreateRagDocumentInput input) {
        String id = Ids.random("rag_");
        List<String> tags = input.tags() == null ? List.of() : input.tags();
        Timestamp now = Timestamp.from(Dates.now());
        jdbc.update(
                connection -> {
                    var ps = connection.prepareStatement(
                            """
                            INSERT INTO rag_documents (id, org_id, title, content, tags, created_at)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """);
                    ps.setString(1, id);
                    ps.setString(2, orgId);
                    ps.setString(3, input.title());
                    ps.setString(4, input.content());
                    ps.setArray(5, connection.createArrayOf("text", tags.toArray(String[]::new)));
                    ps.setTimestamp(6, now);
                    return ps;
                });
        return new RagDocument(id, input.title(), input.content(), tags, Dates.format(now.toInstant()));
    }

    public List<RagSearchHit> searchRagDocuments(String orgId, String query, int limit) {
        int lim = limit <= 0 ? 5 : limit;
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            return List.of();
        }
        List<RagSearchHit> hits = jdbc.query(
                """
                SELECT id, title, content,
                       ts_rank(
                           to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, '')),
                           plainto_tsquery('simple', ?)
                       ) AS score
                FROM rag_documents
                WHERE org_id = ?
                  AND to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, ''))
                      @@ plainto_tsquery('simple', ?)
                ORDER BY score DESC
                LIMIT ?
                """,
                (rs, rowNum) -> new RagSearchHit(
                        rs.getString("id"),
                        rs.getString("title"),
                        RagHelper.snippet(rs.getString("content"), q, 180),
                        rs.getDouble("score")),
                q,
                orgId,
                q,
                lim);
        if (!hits.isEmpty()) {
            return hits;
        }
        String like = "%" + q + "%";
        return jdbc.query(
                """
                SELECT id, title, content FROM rag_documents
                WHERE org_id = ? AND (title ILIKE ? OR content ILIKE ?)
                ORDER BY created_at DESC LIMIT ?
                """,
                (rs, rowNum) -> new RagSearchHit(
                        rs.getString("id"),
                        rs.getString("title"),
                        RagHelper.snippet(rs.getString("content"), q, 180),
                        0.6),
                orgId,
                like,
                like,
                lim);
    }

    private List<String> readTags(java.sql.ResultSet rs) throws SQLException {
        Array arr = rs.getArray("tags");
        if (arr == null) {
            return List.of();
        }
        Object raw = arr.getArray();
        if (raw instanceof String[] strings) {
            return Arrays.asList(strings);
        }
        return List.of();
    }

    private static String formatTimestamp(Timestamp ts) {
        return ts == null ? Dates.format(Dates.now()) : Dates.format(ts.toInstant());
    }
}
