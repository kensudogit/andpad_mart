package jp.andpad.imart.workflow;

/**
 * ワークフロー処理中のビジネスルール違反。
 */
public class WorkflowException extends RuntimeException {

    public WorkflowException(String message) {
        super(message);
    }
}
