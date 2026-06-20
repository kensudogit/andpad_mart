package jp.andpad.imart.auth.http;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import jp.andpad.imart.auth.model.ImLoginSession;
import jp.andpad.imart.auth.spi.IntraMartSessionService;
import lombok.RequiredArgsConstructor;

/**
 * HTTP ブリッジ経由の {@code LoginSessionManager} 実装。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartSessionService implements IntraMartSessionService {

    private final IntraMartSsjsBridgeClient bridgeClient;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ImLoginSession> getLoginSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "platform",
                "LoginSessionManager",
                "getLoginSessionInfo",
                List.of(sessionId),
                sessionId,
                Map.of()));
        if (!response.success() || response.result() == null) {
            return Optional.of(ImLoginSession.invalid(sessionId));
        }
        Map<String, Object> info = (Map<String, Object>) response.result();
        boolean valid = Boolean.TRUE.equals(info.get("valid"));
        if (!valid) {
            return Optional.of(ImLoginSession.invalid(sessionId));
        }
        return Optional.of(new ImLoginSession(
                sessionId,
                stringValue(info.get("accountId")),
                stringValue(info.get("userCode")),
                stringValue(info.get("tenantId")),
                stringValue(info.get("displayName")),
                stringValue(info.get("locale")),
                Instant.now(),
                true));
    }

    @Override
    public void invalidateSession(String sessionId) {
        bridgeClient.invoke(new SsjsInvokeRequest(
                "platform",
                "LoginSessionManager",
                "invalidateLoginSession",
                List.of(sessionId),
                sessionId,
                Map.of()));
    }

    private static String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
