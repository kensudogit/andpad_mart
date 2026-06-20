package jp.andpad.imart.workflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.andpad.imart.workflow.model.WorkflowAssigneeType;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import jp.andpad.imart.workflow.model.WorkflowStepType;

/**
 * 組み込みワークフロー定義レジストリ。
 *
 * <p>任意業務に再利用できる標準フローと、ANDPAD ドメイン向けプリセットを提供する。
 */
public final class WorkflowDefinitionRegistry {

    public static final String FLOW_GENERIC_SINGLE = "generic-single-approval";
    public static final String FLOW_GENERIC_TWO_STEP = "generic-two-step-approval";
    public static final String FLOW_BUDGET = "budget-approval";
    public static final String FLOW_LEAVE = "leave-approval";
    public static final String FLOW_DOC = "document-approval";

    private static final Map<String, WorkflowDefinition> BUILTIN = new LinkedHashMap<>();

    static {
        register(builtin(FLOW_GENERIC_SINGLE, "汎用単段承認", "GENERIC", approvalFlow(
                step("manager_approval", "上長承認", 1, "manager", "node_manager"))));
        register(builtin(FLOW_GENERIC_TWO_STEP, "汎用二段承認", "GENERIC", approvalFlow(
                step("manager_approval", "上長承認", 1, "manager", "node_manager"),
                step("director_approval", "部門長承認", 2, "admin", "node_director"))));
        register(builtin(FLOW_BUDGET, "予算承認", "PROJECT_BUDGET", approvalFlow(
                step("manager_approval", "現場責任者承認", 1, "manager", "node_budget_mgr"),
                step("finance_approval", "経理承認", 2, "admin", "node_budget_fin"))));
        register(builtin(FLOW_LEAVE, "休暇申請", "LEAVE_REQUEST", approvalFlow(
                step("manager_approval", "上長承認", 1, "manager", "node_leave_mgr"))));
        register(builtin(FLOW_DOC, "書類承認", "DOCUMENT", approvalFlow(
                step("reviewer_approval", "レビュー", 1, "manager", "node_doc_review"),
                step("final_approval", "最終承認", 2, "admin", "node_doc_final"))));
    }

    private WorkflowDefinitionRegistry() {}

    public static WorkflowDefinition getBuiltin(String flowId) {
        WorkflowDefinition def = BUILTIN.get(flowId);
        if (def == null) {
            throw new WorkflowException("unknown builtin flow: " + flowId);
        }
        return def;
    }

    public static List<WorkflowDefinition> listBuiltin() {
        return List.copyOf(BUILTIN.values());
    }

    public static void register(WorkflowDefinition definition) {
        BUILTIN.put(definition.flowId(), definition);
    }

    private static WorkflowDefinition builtin(
            String flowId, String name, String entityType, List<WorkflowStepDefinition> approvals) {
        return new WorkflowDefinition(
                "builtin-" + flowId,
                flowId,
                name,
                entityType,
                1,
                "組み込み定義: " + name,
                approvals);
    }

    private static List<WorkflowStepDefinition> approvalFlow(WorkflowStepDefinition... approvals) {
        java.util.ArrayList<WorkflowStepDefinition> steps = new java.util.ArrayList<>();
        steps.add(new WorkflowStepDefinition(
                "submit", "起票", 0, WorkflowStepType.SUBMIT, WorkflowAssigneeType.SUBMITTER, null, null));
        steps.addAll(List.of(approvals));
        steps.add(new WorkflowStepDefinition(
                "complete", "完了", 99, WorkflowStepType.END, WorkflowAssigneeType.ANY, null, null));
        return List.copyOf(steps);
    }

    private static WorkflowStepDefinition step(
            String key, String name, int order, String role, String imNodeId) {
        return new WorkflowStepDefinition(
                key, name, order, WorkflowStepType.APPROVAL, WorkflowAssigneeType.ROLE, role, imNodeId);
    }
}
