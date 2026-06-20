package jp.andpad.imart.workflow;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jp.andpad.imart.auth.model.WorkflowAuthRequest;
import jp.andpad.imart.auth.spi.IntraMartWorkflowAuthService;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowStepDefinition;
import jp.andpad.imart.workflow.spi.WorkflowAuthGuard;

/**
 * intra-mart {@code WorkflowAuthUtil} を利用したワークフロー認可ガード。
 */
@Component
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class ImWorkflowAuthGuard implements WorkflowAuthGuard {

    private final ObjectProvider<IntraMartWorkflowAuthService> workflowAuthService;

    public ImWorkflowAuthGuard(ObjectProvider<IntraMartWorkflowAuthService> workflowAuthService) {
        this.workflowAuthService = workflowAuthService;
    }

    @Override
    public boolean authorize(
            String sessionId,
            WorkflowDefinition definition,
            String systemMatterId,
            String stepKey,
            WorkflowAction action) {
        if (sessionId == null || systemMatterId == null) {
            return true;
        }
        IntraMartWorkflowAuthService auth = workflowAuthService.getIfAvailable();
        if (auth == null) {
            return true;
        }
        String imNodeId = definition.steps().stream()
                .filter(s -> s.stepKey().equals(stepKey))
                .map(WorkflowStepDefinition::imNodeId)
                .findFirst()
                .orElse(null);
        WorkflowAuthRequest request = new WorkflowAuthRequest(
                sessionId, systemMatterId, null, definition.flowId(), imNodeId, null, null, null);
        return switch (action) {
            case SUBMIT -> auth.isAuthApply(request);
            case APPROVE, RETURN -> auth.canProcess(request);
            case REJECT -> auth.isAuthConfirm(request);
            case CANCEL -> auth.isAuthRefDetail(request);
        };
    }
}
