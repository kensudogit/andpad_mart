package jp.andpad.api.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.andpad.api.domain.MonitoringFlowDataView;
import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MonitoringFlowRepository {

    private final JdbcTemplate jdbc;

    public void upsert(String orgId, MonitoringFlowData data) {
        jdbc.update(
                """
                INSERT INTO wf_monitoring_flow_data (
                    id, org_id, flow_id, flow_name,
                    approve_count, approve_end_count, deny_count, discontinue_count, matter_handle_count,
                    minimum_time, maximum_time, average_time, amount_time, count_sum, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                ON CONFLICT (org_id, flow_id) DO UPDATE SET
                    flow_name = EXCLUDED.flow_name,
                    approve_count = EXCLUDED.approve_count,
                    approve_end_count = EXCLUDED.approve_end_count,
                    deny_count = EXCLUDED.deny_count,
                    discontinue_count = EXCLUDED.discontinue_count,
                    matter_handle_count = EXCLUDED.matter_handle_count,
                    minimum_time = EXCLUDED.minimum_time,
                    maximum_time = EXCLUDED.maximum_time,
                    average_time = EXCLUDED.average_time,
                    amount_time = EXCLUDED.amount_time,
                    count_sum = EXCLUDED.count_sum,
                    updated_at = NOW()
                """,
                UUID.randomUUID().toString(),
                orgId,
                data.flowId(),
                data.flowName(),
                data.approveCount(),
                data.approveEndCount(),
                data.denyCount(),
                data.discontinueCount(),
                data.matterHandleCount(),
                data.minimumTime(),
                data.maximumTime(),
                data.averageTime(),
                data.amountTime(),
                data.countSum());
    }

    public List<MonitoringFlowDataView> list(String orgId, List<String> flowIds) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT flow_id, flow_name, approve_count, approve_end_count, deny_count,
                       discontinue_count, matter_handle_count, minimum_time, maximum_time,
                       average_time, amount_time, count_sum
                FROM wf_monitoring_flow_data
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
        sql.append(" ORDER BY flow_id");
        return jdbc.query(sql.toString(), args.toArray(), this::mapRow);
    }

    private MonitoringFlowDataView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MonitoringFlowDataView(
                rs.getString("flow_id"),
                rs.getString("flow_name"),
                rs.getString("approve_count"),
                rs.getString("approve_end_count"),
                rs.getString("deny_count"),
                rs.getString("discontinue_count"),
                rs.getString("matter_handle_count"),
                rs.getString("minimum_time"),
                rs.getString("maximum_time"),
                rs.getString("average_time"),
                rs.getString("amount_time"),
                rs.getString("count_sum"));
    }
}
