package jp.andpad.imart.auth.stub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.model.WorkflowAuthRequest;
import jp.andpad.imart.auth.spi.IntraMartWorkflowAuthService;
import lombok.RequiredArgsConstructor;

/**
 * ローカル開発用 {@code WorkflowAuthUtil} スタブ。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartWorkflowAuthService implements IntraMartWorkflowAuthService {

    private final IntraMartAuthProperties properties;

    @Override
    public boolean isAuthApply(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    @Override
    public boolean isAuthProcess(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    @Override
    public boolean isAuthConfirm(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    @Override
    public boolean isAuthRefDetail(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    @Override
    public boolean canApply(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    @Override
    public boolean canProcess(WorkflowAuthRequest request) {
        return isDevSession(request.sessionId());
    }

    private boolean isDevSession(String sessionId) {
        return properties.getDevSessionId().equals(sessionId);
    }
}
