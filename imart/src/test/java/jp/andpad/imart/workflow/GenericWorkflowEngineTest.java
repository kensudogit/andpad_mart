package jp.andpad.imart.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowActor;
import jp.andpad.imart.workflow.model.WorkflowContext;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;

class GenericWorkflowEngineTest {

    @Test
    void submitsDraftToFirstApprovalStep() {
        WorkflowDefinition def = WorkflowDefinitionRegistry.getBuiltin(
                WorkflowDefinitionRegistry.FLOW_GENERIC_SINGLE);
        WorkflowContext draft = new WorkflowContext(WorkflowInstanceStatus.DRAFT, null, "user1");
        var result = GenericWorkflowEngine.transition(def, draft, WorkflowAction.SUBMIT);
        assertThat(result.nextStatus()).isEqualTo(WorkflowInstanceStatus.RUNNING);
        assertThat(result.taskStepKey()).isEqualTo("manager_approval");
        assertThat(result.createTask()).isTrue();
    }

    @Test
    void approvesThroughTwoStepFlow() {
        WorkflowDefinition def = WorkflowDefinitionRegistry.getBuiltin(
                WorkflowDefinitionRegistry.FLOW_GENERIC_TWO_STEP);
        WorkflowContext running = new WorkflowContext(
                WorkflowInstanceStatus.RUNNING, "manager_approval", "user1");
        var afterManager = GenericWorkflowEngine.transition(def, running, WorkflowAction.APPROVE);
        assertThat(afterManager.nextStatus()).isEqualTo(WorkflowInstanceStatus.RUNNING);
        assertThat(afterManager.taskStepKey()).isEqualTo("director_approval");

        WorkflowContext directorStep = new WorkflowContext(
                WorkflowInstanceStatus.RUNNING, "director_approval", "user1");
        var afterDirector = GenericWorkflowEngine.transition(def, directorStep, WorkflowAction.APPROVE);
        assertThat(afterDirector.nextStatus()).isEqualTo(WorkflowInstanceStatus.APPROVED);
        assertThat(afterDirector.terminal()).isTrue();
    }

    @Test
    void rejectsUnauthorizedActor() {
        WorkflowDefinition def = WorkflowDefinitionRegistry.getBuiltin(
                WorkflowDefinitionRegistry.FLOW_GENERIC_SINGLE);
        WorkflowContext running = new WorkflowContext(
                WorkflowInstanceStatus.RUNNING, "manager_approval", "user1");
        WorkflowActor member = new WorkflowActor("user2", "member", false);
        assertThat(GenericWorkflowEngine.canAct(def, running, member, "manager_approval")).isFalse();
    }

    @Test
    void rejectsInvalidSubmit() {
        WorkflowDefinition def = WorkflowDefinitionRegistry.getBuiltin(
                WorkflowDefinitionRegistry.FLOW_GENERIC_SINGLE);
        WorkflowContext running = new WorkflowContext(WorkflowInstanceStatus.RUNNING, "manager_approval", "user1");
        assertThatThrownBy(() -> GenericWorkflowEngine.transition(def, running, WorkflowAction.SUBMIT))
                .isInstanceOf(WorkflowException.class);
    }
}
