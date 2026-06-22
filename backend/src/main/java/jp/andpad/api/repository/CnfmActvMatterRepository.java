package jp.andpad.api.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.andpad.api.domain.CnfmActvMatterView;
import jp.andpad.api.util.Dates;
import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CnfmActvMatterRepository {

    private final JdbcTemplate jdbc;

    public void upsert(String orgId, ActvMatterCnfmData data) {
        jdbc.update(
                """
                INSERT INTO wf_cnfm_actv_matters (
                    id, org_id, list_type, system_matter_id, flow_id, flow_name, matter_name, matter_number,
                    node_id, apply_auth_user_code, apply_auth_user_name, apply_date, arrived_date,
                    confirm_cpl_flag, priority_level, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                ON CONFLICT (org_id, list_type, system_matter_id) DO UPDATE SET
                    flow_id = EXCLUDED.flow_id,
                    flow_name = EXCLUDED.flow_name,
                    matter_name = EXCLUDED.matter_name,
                    matter_number = EXCLUDED.matter_number,
                    node_id = EXCLUDED.node_id,
                    apply_auth_user_code = EXCLUDED.apply_auth_user_code,
                    apply_auth_user_name = EXCLUDED.apply_auth_user_name,
                    apply_date = EXCLUDED.apply_date,
                    arrived_date = EXCLUDED.arrived_date,
                    confirm_cpl_flag = EXCLUDED.confirm_cpl_flag,
                    priority_level = EXCLUDED.priority_level,
                    updated_at = NOW()
                """,
                UUID.randomUUID().toString(),
                orgId,
                data.listType(),
                data.systemMatterId(),
                data.flowId(),
                data.flowName(),
                data.matterName(),
                data.matterNumber(),
                data.nodeId(),
                data.applyAuthUserCode(),
                data.applyAuthUserName(),
                data.applyDate(),
                data.arrivedDate(),
                data.confirmCplFlag(),
                data.priorityLevel());
    }

    public List<CnfmActvMatterView> list(String orgId, String listType, List<String> flowIds, Integer limit) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, list_type, system_matter_id, flow_id, flow_name, matter_name, matter_number,
                       node_id, apply_auth_user_code, apply_auth_user_name, apply_date, arrived_date,
                       confirm_cpl_flag, priority_level, updated_at
                FROM wf_cnfm_actv_matters
                WHERE org_id = ? AND list_type = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        args.add(listType);
        if (flowIds != null && !flowIds.isEmpty()) {
            sql.append(" AND flow_id IN (");
            sql.append(String.join(", ", flowIds.stream().map(f -> "?").toList()));
            sql.append(')');
            args.addAll(flowIds);
        }
        sql.append(" ORDER BY updated_at DESC");
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
            args.add(limit);
        }
        return jdbc.query(sql.toString(), args.toArray(), this::mapRow);
    }

    private CnfmActvMatterView mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new CnfmActvMatterView(
                rs.getString("id"),
                rs.getString("list_type"),
                rs.getString("system_matter_id"),
                rs.getString("flow_id"),
                rs.getString("flow_name"),
                rs.getString("matter_name"),
                rs.getString("matter_number"),
                rs.getString("node_id"),
                rs.getString("apply_auth_user_code"),
                rs.getString("apply_auth_user_name"),
                rs.getString("apply_date"),
                rs.getString("arrived_date"),
                rs.getString("confirm_cpl_flag"),
                rs.getString("priority_level"),
                updatedAt != null ? Dates.format(updatedAt.toInstant()) : null);
    }
}
