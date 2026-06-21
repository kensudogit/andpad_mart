package jp.andpad.imart.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.spi.IntraMartMailService;
import jp.andpad.imart.workflow.WorkflowDefinitionRegistry;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;

@ExtendWith(MockitoExtension.class)
class WorkflowMailNotifierTest {

    @Mock
    private IntraMartMailProperties properties;

    @Mock
    private IntraMartMailService mailService;

    @InjectMocks
    private WorkflowMailNotifier notifier;

    @Test
    void notifyTransitionUsesDocumentApprovalSubmitTemplate() {
        WorkflowDefinition definition =
                WorkflowDefinitionRegistry.getBuiltin(WorkflowDefinitionRegistry.FLOW_DOC);
        IntraMartMailProperties.WorkflowMailTemplates templates = new IntraMartMailProperties.WorkflowMailTemplates();
        templates.setSubmitMailId("andpad-doc-submit");

        when(properties.isEnabled()).thenReturn(true);
        when(properties.getLocaleId()).thenReturn("ja");
        when(properties.getWorkflow()).thenReturn(Map.of("document-approval", templates));
        when(mailService.send(any())).thenReturn(MailSendResult.ok("subject", "body"));

        WorkflowStepDefinition submitStep = definition.stepByKey("submit");
        notifier.notifyTransition(
                definition,
                "inst-1",
                "安全書類",
                "申請者",
                submitStep,
                WorkflowAction.SUBMIT,
                WorkflowInstanceStatus.RUNNING,
                "reviewer_approval",
                "申請します",
                "dev-imart-session");

        ArgumentCaptor<MailSendRequest> captor = ArgumentCaptor.forClass(MailSendRequest.class);
        verify(mailService).send(captor.capture());
        assertThat(captor.getValue().mailId()).isEqualTo("andpad-doc-submit");
        assertThat(captor.getValue().parameters()).containsEntry("title", "安全書類");
    }
}
