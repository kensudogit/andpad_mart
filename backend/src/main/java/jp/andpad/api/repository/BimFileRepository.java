package jp.andpad.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BimFileRepository {

    private final JdbcTemplate jdbc;

    @Value("${app.bim.store-in-database:true}")
    private boolean storeInDatabase;

    public void save(String orgId, String storedName, String fileKind, String contentType, byte[] data) {
        if (!storeInDatabase) {
            return;
        }
        jdbc.update(
                """
                INSERT INTO bim_uploaded_files (id, org_id, stored_name, file_kind, content_type, data, size_bytes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (org_id, stored_name) DO UPDATE SET
                    file_kind = EXCLUDED.file_kind,
                    content_type = EXCLUDED.content_type,
                    data = EXCLUDED.data,
                    size_bytes = EXCLUDED.size_bytes,
                    created_at = NOW()
                """,
                UUID.randomUUID().toString(),
                orgId,
                storedName,
                fileKind,
                contentType,
                data,
                data.length);
    }

    public Optional<byte[]> find(String orgId, String storedName) {
        if (!storeInDatabase) {
            return Optional.empty();
        }
        return jdbc.query(
                "SELECT data FROM bim_uploaded_files WHERE org_id = ? AND stored_name = ?",
                rs -> rs.next() ? Optional.of(rs.getBytes("data")) : Optional.empty(),
                orgId,
                storedName);
    }
}
