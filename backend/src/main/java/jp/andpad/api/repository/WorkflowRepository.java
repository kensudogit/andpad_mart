package jp.andpad.api.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.andpad.api.domain.WorkflowTypes.WorkflowDefinitionView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowHistoryView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowInstanceView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowStepView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowTaskView;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowAssigneeType;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import jp.andpad.imart.workflow.model.WorkflowStepType;
import jp.andpad.imart.workflow.model.WorkflowTaskStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkflowRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public List<WorkflowDefinitionView> listDefinitions(String orgId) {
        List<WorkflowDefinitionView> defs = jdbc.query(
                """
                SELECT id, flow_id, name, entity_type, version, description
                FROM wf_definitions
                WHERE org_id = ? AND active = TRUE
                ORDER BY entity_type, flow_id
                """,
                (rs, rowNum) -> mapDefinitionHeader(rs),
                orgId);
        return defs.stream()
                .map(d -> new WorkflowDefinitionView(
                        d.id(), d.flowId(), d.name(), d.entityType(), d.version(), d.description(), loadSteps(d.id())))
                .toList();
    }

    public Optional<WorkflowDefinitionView> findDefinitionByFlowId(String orgId, String flowId) {
        try {
            WorkflowDefinitionView header = jdbc.queryForObject(
                    """
                    SELECT id, flow_id, name, entity_type, version, description
                    FROM wf_definitions
                    WHERE org_id = ? AND flow_id = ? AND active = TRUE
                    ORDER BY version DESC
                    LIMIT 1
                    """,
                    (rs, rowNum) -> mapDefinitionHeader(rs),
                    orgId,
                    flowId);
            return Optional.of(new WorkflowDefinitionView(
                    header.id(),
                    header.flowId(),
                    header.name(),
                    header.entityType(),
                    header.version(),
                    header.description(),
                    loadSteps(header.id())));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public WorkflowDefinition loadEngineDefinition(String definitionId) {
        WorkflowDefinitionView view = jdbc.queryForObject(
                """
                SELECT id, flow_id, name, entity_type, version, description
                FROM wf_definitions WHERE id = ?
                """,
                (rs, rowNum) -> mapDefinitionHeader(rs),
                definitionId);
        List<WorkflowStepView> steps = loadSteps(definitionId);
        List<WorkflowStepDefinition> engineSteps = steps.stream()
                .map(s -> new WorkflowStepDefinition(
                        s.stepKey(),
                        s.name(),
                        s.stepOrder(),
                        WorkflowStepType.valueOf(s.stepType()),
                        WorkflowAssigneeType.valueOf(s.assigneeType()),
                        s.assigneeValue(),
                        s.imNodeId()))
                .toList();
        return new WorkflowDefinition(
                view.id(),
                view.flowId(),
                view.name(),
                view.entityType(),
                view.version(),
                view.description(),
                engineSteps);
    }

    public List<WorkflowInstanceView> listInstances(String orgId, String entityType, String status) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT i.id, i.definition_id, d.flow_id, d.name AS flow_name,
                       i.entity_type, i.entity_id, i.status, i.current_step_key, i.title,
                       i.payload, i.submitter_user_id, i.im_system_matter_id,
                       i.created_at, i.updated_at, i.completed_at
                FROM wf_instances i
                JOIN wf_definitions d ON d.id = i.definition_id
                WHERE i.org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (entityType != null && !entityType.isBlank()) {
            sql.append(" AND i.entity_type = ?");
            args.add(entityType);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND i.status = ?");
            args.add(status);
        }
        sql.append(" ORDER BY i.updated_at DESC");
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapInstance(rs), args.toArray()).stream()
                .map(this::fillInstanceDetails)
                .toList();
    }

    public Optional<WorkflowInstanceView> findInstance(String orgId, String instanceId) {
        try {
            WorkflowInstanceView instance = jdbc.queryForObject(
                    """
                    SELECT i.id, i.definition_id, d.flow_id, d.name AS flow_name,
                           i.entity_type, i.entity_id, i.status, i.current_step_key, i.title,
                           i.payload, i.submitter_user_id, i.im_system_matter_id,
                           i.created_at, i.updated_at, i.completed_at
                    FROM wf_instances i
                    JOIN wf_definitions d ON d.id = i.definition_id
                    WHERE i.org_id = ? AND i.id = ?
                    """,
                    (rs, rowNum) -> mapInstance(rs),
                    orgId,
                    instanceId);
            return Optional.of(fillInstanceDetails(instance));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<WorkflowTaskView> listMyPendingTasks(String orgId, String userId, String role, boolean admin) {
        List<WorkflowTaskView> all = jdbc.query(
                """
                SELECT t.id, t.instance_id, t.step_key, s.name AS step_name,
                       t.assignee_type, t.assignee_value, t.status, t.action_taken,
                       t.comment, t.acted_by_user_id, t.created_at, t.completed_at
                FROM wf_tasks t
                JOIN wf_instances i ON i.id = t.instance_id
                LEFT JOIN wf_steps s ON s.definition_id = i.definition_id AND s.step_key = t.step_key
                WHERE t.org_id = ? AND t.status = 'PENDING' AND i.status = 'RUNNING'
                ORDER BY t.created_at ASC
                """,
                (rs, rowNum) -> mapTask(rs),
                orgId);
        return all.stream().filter(t -> admin || matchesTask(t, userId, role)).toList();
    }

    @Transactional
    public WorkflowInstanceView createInstance(
            String orgId,
            String definitionId,
            String entityType,
            String entityId,
            String title,
            Map<String, Object> payload,
            String submitterUserId,
            String imSystemMatterId) {
        String id = Ids.random("wfi_");
        jdbc.update(
                """
                INSERT INTO wf_instances (
                    id, org_id, definition_id, entity_type, entity_id, status,
                    current_step_key, title, payload, submitter_user_id, im_system_matter_id
                ) VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?::jsonb, ?, ?)
                """,
                id,
                orgId,
                definitionId,
                entityType,
                entityId,
                WorkflowInstanceStatus.DRAFT.name(),
                title,
                toJson(payload),
                submitterUserId,
                imSystemMatterId);
        addHistory(orgId, id, null, "CREATE", submitterUserId, submitterUserId, "instance created", Map.of());
        return findInstance(orgId, id).orElseThrow();
    }

    @Transactional
    public WorkflowInstanceView updateInstanceState(
            String orgId,
            String instanceId,
            WorkflowInstanceStatus status,
            String currentStepKey,
            boolean completed) {
        if (completed) {
            jdbc.update(
                    """
                    UPDATE wf_instances
                    SET status = ?, current_step_key = ?, updated_at = NOW(), completed_at = NOW()
                    WHERE org_id = ? AND id = ?
                    """,
                    status.name(),
                    currentStepKey,
                    orgId,
                    instanceId);
        } else {
            jdbc.update(
                    """
                    UPDATE wf_instances
                    SET status = ?, current_step_key = ?, updated_at = NOW()
                    WHERE org_id = ? AND id = ?
                    """,
                    status.name(),
                    currentStepKey,
                    orgId,
                    instanceId);
        }
        return findInstance(orgId, instanceId).orElseThrow();
    }

    @Transactional
    public WorkflowTaskView createTask(
            String orgId,
            String instanceId,
            String stepKey,
            WorkflowAssigneeType assigneeType,
            String assigneeValue) {
        skipPendingTasks(orgId, instanceId);
        String id = Ids.random("wft_");
        jdbc.update(
                """
                INSERT INTO wf_tasks (
                    id, org_id, instance_id, step_key, assignee_type, assignee_value, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                instanceId,
                stepKey,
                assigneeType.name(),
                assigneeValue,
                WorkflowTaskStatus.PENDING.name());
        return findTask(orgId, id).orElseThrow();
    }

    @Transactional
    public WorkflowTaskView completeTask(
            String orgId,
            String taskId,
            WorkflowAction action,
            String actedByUserId,
            String comment) {
        jdbc.update(
                """
                UPDATE wf_tasks
                SET status = ?, action_taken = ?, comment = ?, acted_by_user_id = ?, completed_at = NOW()
                WHERE org_id = ? AND id = ?
                """,
                WorkflowTaskStatus.COMPLETED.name(),
                action.name(),
                comment,
                actedByUserId,
                orgId,
                taskId);
        return findTask(orgId, taskId).orElseThrow();
    }

    @Transactional
    public void addHistory(
            String orgId,
            String instanceId,
            String stepKey,
            String action,
            String actorUserId,
            String actorName,
            String comment,
            Map<String, Object> metadata) {
        jdbc.update(
                """
                INSERT INTO wf_history (
                    id, org_id, instance_id, step_key, action, actor_user_id, actor_name, comment, metadata
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                """,
                Ids.random("wfh_"),
                orgId,
                instanceId,
                stepKey,
                action,
                actorUserId,
                actorName,
                comment,
                toJson(metadata));
    }

    public Optional<WorkflowTaskView> findPendingTask(String orgId, String instanceId, String stepKey) {
        try {
            return Optional.of(jdbc.queryForObject(
                    """
                    SELECT t.id, t.instance_id, t.step_key, s.name AS step_name,
                           t.assignee_type, t.assignee_value, t.status, t.action_taken,
                           t.comment, t.acted_by_user_id, t.created_at, t.completed_at
                    FROM wf_tasks t
                    JOIN wf_instances i ON i.id = t.instance_id
                    LEFT JOIN wf_steps s ON s.definition_id = i.definition_id AND s.step_key = t.step_key
                    WHERE t.org_id = ? AND t.instance_id = ? AND t.step_key = ? AND t.status = 'PENDING'
                    """,
                    (rs, rowNum) -> mapTask(rs),
                    orgId,
                    instanceId,
                    stepKey));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<WorkflowTaskView> findTask(String orgId, String taskId) {
        try {
            return Optional.of(jdbc.queryForObject(
                    """
                    SELECT t.id, t.instance_id, t.step_key, s.name AS step_name,
                           t.assignee_type, t.assignee_value, t.status, t.action_taken,
                           t.comment, t.acted_by_user_id, t.created_at, t.completed_at
                    FROM wf_tasks t
                    JOIN wf_instances i ON i.id = t.instance_id
                    LEFT JOIN wf_steps s ON s.definition_id = i.definition_id AND s.step_key = t.step_key
                    WHERE t.org_id = ? AND t.id = ?
                    """,
                    (rs, rowNum) -> mapTask(rs),
                    orgId,
                    taskId));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private void skipPendingTasks(String orgId, String instanceId) {
        jdbc.update(
                """
                UPDATE wf_tasks SET status = ?, completed_at = NOW()
                WHERE org_id = ? AND instance_id = ? AND status = ?
                """,
                WorkflowTaskStatus.SKIPPED.name(),
                orgId,
                instanceId,
                WorkflowTaskStatus.PENDING.name());
    }

    private List<WorkflowStepView> loadSteps(String definitionId) {
        return jdbc.query(
                """
                SELECT step_key, name, step_order, step_type, assignee_type, assignee_value, im_node_id
                FROM wf_steps WHERE definition_id = ? ORDER BY step_order
                """,
                (rs, rowNum) -> new WorkflowStepView(
                        rs.getString("step_key"),
                        rs.getString("name"),
                        rs.getInt("step_order"),
                        rs.getString("step_type"),
                        rs.getString("assignee_type"),
                        rs.getString("assignee_value"),
                        rs.getString("im_node_id")),
                definitionId);
    }

    private WorkflowInstanceView fillInstanceDetails(WorkflowInstanceView instance) {
        List<WorkflowTaskView> tasks = jdbc.query(
                """
                SELECT t.id, t.instance_id, t.step_key, s.name AS step_name,
                       t.assignee_type, t.assignee_value, t.status, t.action_taken,
                       t.comment, t.acted_by_user_id, t.created_at, t.completed_at
                FROM wf_tasks t
                JOIN wf_instances i ON i.id = t.instance_id
                LEFT JOIN wf_steps s ON s.definition_id = i.definition_id AND s.step_key = t.step_key
                WHERE t.instance_id = ?
                ORDER BY t.created_at
                """,
                (rs, rowNum) -> mapTask(rs),
                instance.id());
        List<WorkflowHistoryView> history = jdbc.query(
                """
                SELECT id, step_key, action, actor_user_id, actor_name, comment, created_at
                FROM wf_history WHERE instance_id = ? ORDER BY created_at
                """,
                (rs, rowNum) -> new WorkflowHistoryView(
                        rs.getString("id"),
                        rs.getString("step_key"),
                        rs.getString("action"),
                        rs.getString("actor_user_id"),
                        rs.getString("actor_name"),
                        rs.getString("comment"),
                        Dates.format(rs.getTimestamp("created_at").toInstant())),
                instance.id());
        return new WorkflowInstanceView(
                instance.id(),
                instance.definitionId(),
                instance.flowId(),
                instance.flowName(),
                instance.entityType(),
                instance.entityId(),
                instance.status(),
                instance.currentStepKey(),
                instance.title(),
                instance.payload(),
                instance.submitterUserId(),
                instance.imSystemMatterId(),
                tasks,
                history,
                instance.createdAt(),
                instance.updatedAt(),
                instance.completedAt());
    }

    private static boolean matchesTask(WorkflowTaskView task, String userId, String role) {
        return switch (WorkflowAssigneeType.valueOf(task.assigneeType())) {
            case USER -> userId.equals(task.assigneeValue());
            case ROLE -> role != null && role.equalsIgnoreCase(task.assigneeValue());
            case SUBMITTER, ANY -> true;
        };
    }

    private WorkflowDefinitionView mapDefinitionHeader(ResultSet rs) throws SQLException {
        return new WorkflowDefinitionView(
                rs.getString("id"),
                rs.getString("flow_id"),
                rs.getString("name"),
                rs.getString("entity_type"),
                rs.getInt("version"),
                rs.getString("description"),
                List.of());
    }

    private WorkflowInstanceView mapInstance(ResultSet rs) throws SQLException {
        return new WorkflowInstanceView(
                rs.getString("id"),
                rs.getString("definition_id"),
                rs.getString("flow_id"),
                rs.getString("flow_name"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("status"),
                rs.getString("current_step_key"),
                rs.getString("title"),
                rs.getString("payload"),
                rs.getString("submitter_user_id"),
                rs.getString("im_system_matter_id"),
                List.of(),
                List.of(),
                formatTs(rs.getTimestamp("created_at")),
                formatTs(rs.getTimestamp("updated_at")),
                formatTs(rs.getTimestamp("completed_at")));
    }

    private WorkflowTaskView mapTask(ResultSet rs) throws SQLException {
        return new WorkflowTaskView(
                rs.getString("id"),
                rs.getString("instance_id"),
                rs.getString("step_key"),
                rs.getString("step_name"),
                rs.getString("assignee_type"),
                rs.getString("assignee_value"),
                rs.getString("status"),
                rs.getString("action_taken"),
                rs.getString("comment"),
                rs.getString("acted_by_user_id"),
                formatTs(rs.getTimestamp("created_at")),
                formatTs(rs.getTimestamp("completed_at")));
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map != null ? map : Map.of());
        } catch (Exception ex) {
            return "{}";
        }
    }

    private static String formatTs(Timestamp ts) {
        if (ts == null) {
            return null;
        }
        return Dates.format(ts.toInstant());
    }
}
