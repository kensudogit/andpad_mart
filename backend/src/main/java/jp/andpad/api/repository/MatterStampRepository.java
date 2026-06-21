package jp.andpad.api.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.andpad.api.domain.MatterStampView;
import jp.andpad.imart.stamp.model.MatterStampData;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MatterStampRepository {

    private final JdbcTemplate jdbc;

    public void insert(String orgId, MatterStampData stamp) {
        jdbc.update(
                """
                INSERT INTO wf_matter_stamps (
                    id, org_id, system_matter_id, stamp_no, node_id, process_date, process_id,
                    stamp_str1, stamp_str1_type, stamp_str2, stamp_str2_type, stamp_str3, stamp_str3_type,
                    stamp_type, cancel_flag, flow_id, entity_type, entity_id, workflow_instance_id, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """,
                UUID.randomUUID().toString(),
                orgId,
                stamp.systemMatterId(),
                stamp.no(),
                stamp.nodeId(),
                stamp.processDate(),
                stamp.processId(),
                stamp.stampStr1(),
                stamp.stampStr1Type(),
                stamp.stampStr2(),
                stamp.stampStr2Type(),
                stamp.stampStr3(),
                stamp.stampStr3Type(),
                stamp.stampType(),
                stamp.cancelFlag(),
                stamp.flowId(),
                stamp.entityType(),
                stamp.entityId(),
                stamp.workflowInstanceId());
    }

    public List<MatterStampView> list(
            String orgId, String flowId, String entityType, String entityId, String systemMatterId, Integer limit) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, system_matter_id, stamp_no, node_id, process_date, process_id,
                       stamp_str1, stamp_str1_type, stamp_str2, stamp_str2_type, stamp_str3, stamp_str3_type,
                       stamp_type, cancel_flag, flow_id, entity_type, entity_id, workflow_instance_id, created_at
                FROM wf_matter_stamps
                WHERE org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (flowId != null && !flowId.isBlank()) {
            sql.append(" AND flow_id = ?");
            args.add(flowId);
        }
        if (entityType != null && !entityType.isBlank()) {
            sql.append(" AND entity_type = ?");
            args.add(entityType);
        }
        if (entityId != null && !entityId.isBlank()) {
            sql.append(" AND entity_id = ?");
            args.add(entityId);
        }
        if (systemMatterId != null && !systemMatterId.isBlank()) {
            sql.append(" AND system_matter_id = ?");
            args.add(systemMatterId);
        }
        sql.append(" ORDER BY created_at ASC, stamp_no ASC");
        int resolvedLimit = limit != null && limit > 0 ? limit : 50;
        sql.append(" LIMIT ?");
        args.add(resolvedLimit);
        return jdbc.query(sql.toString(), args.toArray(), this::mapRow);
    }

    private MatterStampView mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new MatterStampView(
                rs.getString("id"),
                rs.getString("system_matter_id"),
                rs.getString("stamp_no"),
                rs.getString("node_id"),
                rs.getString("process_date"),
                rs.getString("process_id"),
                rs.getString("stamp_str1"),
                rs.getString("stamp_str1_type"),
                rs.getString("stamp_str2"),
                rs.getString("stamp_str2_type"),
                rs.getString("stamp_str3"),
                rs.getString("stamp_str3_type"),
                rs.getString("stamp_type"),
                rs.getString("cancel_flag"),
                rs.getString("flow_id"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("workflow_instance_id"),
                createdAt != null ? createdAt.toInstant().toString() : null);
    }
}
