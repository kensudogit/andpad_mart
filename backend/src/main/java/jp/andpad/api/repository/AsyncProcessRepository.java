package jp.andpad.api.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.andpad.api.domain.AsyncProcessStatusView;
import jp.andpad.api.util.Dates;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AsyncProcessRepository {

    private final JdbcTemplate jdbc;

    public void upsert(
            String orgId,
            AsyncProcessStatusData data,
            String workflowInstanceId,
            String entityType,
            String entityId) {
        jdbc.update(
                """
                INSERT INTO wf_async_process_status (
                    id, org_id, accept_id, async_proc_status, auth_user_code, execute_user_code,
                    flow_id, matter_name, matter_number, message, node_id, proc_comment, proc_date,
                    proc_type, queue_id, sub_message, system_matter_id,
                    entity_type, entity_id, workflow_instance_id, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                ON CONFLICT (org_id, accept_id) DO UPDATE SET
                    async_proc_status = EXCLUDED.async_proc_status,
                    auth_user_code = EXCLUDED.auth_user_code,
                    execute_user_code = EXCLUDED.execute_user_code,
                    flow_id = EXCLUDED.flow_id,
                    matter_name = EXCLUDED.matter_name,
                    matter_number = EXCLUDED.matter_number,
                    message = EXCLUDED.message,
                    node_id = EXCLUDED.node_id,
                    proc_comment = EXCLUDED.proc_comment,
                    proc_date = EXCLUDED.proc_date,
                    proc_type = EXCLUDED.proc_type,
                    queue_id = EXCLUDED.queue_id,
                    sub_message = EXCLUDED.sub_message,
                    system_matter_id = EXCLUDED.system_matter_id,
                    entity_type = COALESCE(EXCLUDED.entity_type, wf_async_process_status.entity_type),
                    entity_id = COALESCE(EXCLUDED.entity_id, wf_async_process_status.entity_id),
                    workflow_instance_id = COALESCE(EXCLUDED.workflow_instance_id, wf_async_process_status.workflow_instance_id),
                    updated_at = NOW()
                """,
                UUID.randomUUID().toString(),
                orgId,
                data.acceptId(),
                data.asyncProcStatus(),
                data.authUserCode(),
                data.executeUserCode(),
                data.flowId(),
                data.matterName(),
                data.matterNumber(),
                data.message(),
                data.nodeId(),
                data.procComment(),
                data.procDate(),
                data.procType(),
                data.queueId(),
                data.subMessage(),
                data.systemMatterId(),
                entityType,
                entityId,
                workflowInstanceId);
    }

    public List<AsyncProcessStatusView> list(
            String orgId, List<String> flowIds, List<String> systemMatterIds, Integer limit) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, accept_id, async_proc_status, auth_user_code, execute_user_code,
                       flow_id, matter_name, matter_number, message, node_id, proc_comment, proc_date,
                       proc_type, queue_id, sub_message, system_matter_id,
                       entity_type, entity_id, workflow_instance_id, updated_at
                FROM wf_async_process_status
                WHERE org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (flowIds != null && !flowIds.isEmpty()) {
            sql.append(" AND flow_id IN (");
            sql.append(String.join(", ", flowIds.stream().map(f -> "?").toList()));
            sql.append(')');
            args.addAll(flowIds);
        }
        if (systemMatterIds != null && !systemMatterIds.isEmpty()) {
            sql.append(" AND system_matter_id IN (");
            sql.append(String.join(", ", systemMatterIds.stream().map(f -> "?").toList()));
            sql.append(')');
            args.addAll(systemMatterIds);
        }
        sql.append(" ORDER BY updated_at DESC");
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
            args.add(limit);
        }
        return jdbc.query(sql.toString(), args.toArray(), this::mapRow);
    }

    private AsyncProcessStatusView mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new AsyncProcessStatusView(
                rs.getString("id"),
                rs.getString("accept_id"),
                rs.getString("async_proc_status"),
                rs.getString("auth_user_code"),
                rs.getString("execute_user_code"),
                rs.getString("flow_id"),
                rs.getString("matter_name"),
                rs.getString("matter_number"),
                rs.getString("message"),
                rs.getString("node_id"),
                rs.getString("proc_comment"),
                rs.getString("proc_date"),
                rs.getString("proc_type"),
                rs.getString("queue_id"),
                rs.getString("sub_message"),
                rs.getString("system_matter_id"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("workflow_instance_id"),
                updatedAt != null ? Dates.format(updatedAt.toInstant()) : null);
    }
}
