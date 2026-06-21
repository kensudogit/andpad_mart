package jp.andpad.imart.stamp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import jp.andpad.imart.stamp.stub.InMemoryMatterStampStore;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowAssigneeType;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import jp.andpad.imart.workflow.model.WorkflowStepType;

class WorkflowStampRecorderTest {

    @Test
    void recordsStampOnTerminalTransition() {
        IntraMartStampProperties properties = new IntraMartStampProperties();
        InMemoryMatterStampStore store = new InMemoryMatterStampStore();
        MatterStampPersister persister = new MatterStampPersister(properties, new NoopMatterStampRecorder());
        WorkflowStampRecorder recorder = new WorkflowStampRecorder(
                properties, store, persister, new MatterStampFileWriter(properties));

        WorkflowDefinition definition = new WorkflowDefinition(
                "def-1",
                "document-approval",
                "書類承認",
                "DOCUMENT",
                1,
                "test",
                List.of(
                        new WorkflowStepDefinition(
                                "submit", "起票", 0, WorkflowStepType.SUBMIT, WorkflowAssigneeType.SUBMITTER, null, null),
                        new WorkflowStepDefinition(
                                "review", "レビュー", 1, WorkflowStepType.APPROVAL, WorkflowAssigneeType.ROLE, "manager", null),
                        new WorkflowStepDefinition(
                                "final", "最終承認", 2, WorkflowStepType.APPROVAL, WorkflowAssigneeType.ROLE, "admin", null),
                        new WorkflowStepDefinition("complete", "完了", 3, WorkflowStepType.END, WorkflowAssigneeType.ANY, null, null)));

        recorder.recordCompletion(
                definition,
                "inst-1",
                "DOCUMENT",
                "rec-1",
                WorkflowAction.APPROVE,
                WorkflowInstanceStatus.APPROVED,
                "final",
                "佐藤 花子",
                null,
                "dev-imart-session");

        assertThat(store.countStamps("wf-inst-1")).isEqualTo(1);
        assertThat(store.listStamps("wf-inst-1").get(0).stampStr1()).isEqualTo("佐藤 花子");
        assertThat(store.listFrames("wf-inst-1")).hasSize(4);
    }
}
