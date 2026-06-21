package jp.andpad.imart.mail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.spi.IntraMartMailService;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ワークフロー遷移時のメール通知（{@code MailTemplateManager} テンプレート利用）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowMailNotifier {

    private final IntraMartMailProperties properties;
    private final IntraMartMailService mailService;

    /**
     * ワークフロー遷移に応じて通知メールを送信する。
     *
     * <p>送信失敗はログ出力のみ（ワークフロー本体はロールバックしない）。
     */
    public void notifyTransition(
            WorkflowDefinition definition,
            String instanceId,
            String title,
            String submitterName,
            WorkflowStepDefinition currentStep,
            WorkflowAction action,
            WorkflowInstanceStatus nextStatus,
            String nextStepKey,
            String comment,
            String imSessionId) {
        if (!properties.isEnabled()) {
            return;
        }
        String mailId = resolveMailId(definition.flowId(), action, nextStatus);
        if (mailId == null || mailId.isBlank()) {
            return;
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("title", nullToEmpty(title));
        params.put("flowName", nullToEmpty(definition.name()));
        params.put("flowId", nullToEmpty(definition.flowId()));
        params.put("instanceId", nullToEmpty(instanceId));
        params.put("submitterName", nullToEmpty(submitterName));
        params.put("stepName", currentStep != null ? nullToEmpty(currentStep.name()) : "");
        params.put("stepKey", currentStep != null ? nullToEmpty(currentStep.stepKey()) : "");
        params.put("action", action.name());
        params.put("nextStatus", nextStatus.name());
        params.put("nextStepKey", nullToEmpty(nextStepKey));
        params.put("comment", nullToEmpty(comment));

        var result = mailService.send(new MailSendRequest(
                imSessionId,
                mailId,
                null,
                properties.getLocaleId(),
                null,
                null,
                params));
        if (!result.sent()) {
            log.warn(
                    "workflow mail not sent: flowId={} mailId={} error={}",
                    definition.flowId(),
                    mailId,
                    result.error());
        }
    }

    private String resolveMailId(String flowId, WorkflowAction action, WorkflowInstanceStatus nextStatus) {
        IntraMartMailProperties.WorkflowMailTemplates templates = properties.getWorkflow().get(flowId);
        if (templates == null) {
            return null;
        }
        return switch (action) {
            case SUBMIT -> templates.getSubmitMailId();
            case APPROVE -> nextStatus == WorkflowInstanceStatus.APPROVED
                    ? templates.getApprovedMailId()
                    : templates.getTaskMailId();
            case REJECT -> templates.getRejectedMailId();
            case RETURN -> templates.getReturnedMailId();
            case CANCEL -> templates.getRejectedMailId();
        };
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
