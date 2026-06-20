package jp.andpad.imart.auth.stub;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.model.ImLoginSession;
import jp.andpad.imart.auth.spi.IntraMartSessionService;
import lombok.RequiredArgsConstructor;

/**
 * ローカル開発用 {@code LoginSessionManager} スタブ。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartSessionService implements IntraMartSessionService {

    private final IntraMartAuthProperties properties;

    @Override
    public Optional<ImLoginSession> getLoginSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        if (!properties.getDevSessionId().equals(sessionId)) {
            return Optional.of(ImLoginSession.invalid(sessionId));
        }
        return Optional.of(new ImLoginSession(
                sessionId,
                properties.getDevAccountId(),
                properties.getDevUserCode(),
                properties.getDevTenantId(),
                "Dev User",
                "ja_JP",
                Instant.now(),
                true));
    }

    @Override
    public void invalidateSession(String sessionId) {
        // no-op in stub mode
    }
}
