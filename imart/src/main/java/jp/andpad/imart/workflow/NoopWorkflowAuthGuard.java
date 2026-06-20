package jp.andpad.imart.workflow;

import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.spi.WorkflowAuthGuard;

/**
 * 常に許可する noop ワークフロー認可ガード（IM 連携無効時）。
 */
public class NoopWorkflowAuthGuard implements WorkflowAuthGuard {

    @Override
    public boolean authorize(
            String sessionId,
            WorkflowDefinition definition,
            String systemMatterId,
            String stepKey,
            WorkflowAction action) {
        return true;
    }
}
