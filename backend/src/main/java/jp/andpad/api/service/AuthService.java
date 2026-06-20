package jp.andpad.api.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import jp.andpad.api.auth.dto.AuthResponse;
import jp.andpad.api.domain.Organization;
import jp.andpad.api.domain.Session;
import jp.andpad.api.repository.AuthRepository;
import jp.andpad.api.repository.AuthRepository.LoginResult;
import jp.andpad.api.repository.AuthRepository.RegisterInput;
import jp.andpad.api.security.AuthPrincipal;
import jp.andpad.api.security.JwtService;
import jp.andpad.api.security.TenantContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final OrganizationService organizationService;

    public AuthResponse login(String email, String password) {
        LoginResult result = authRepository.login(email, password)
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        return toAuthResponse(result);
    }

    public AuthResponse register(RegisterInput input) {
        LoginResult result = authRepository.register(input);
        return toAuthResponse(result);
    }

    public Session currentSession() {
        Optional<AuthPrincipal> principal = TenantContext.principal();
        if (principal.isEmpty()) {
            return null;
        }
        AuthPrincipal p = principal.get();
        Optional<LoginResult> result = authRepository.sessionByUser(p.userId(), p.orgId());
        return result.map(this::toSession).orElse(null);
    }

    private AuthResponse toAuthResponse(LoginResult result) {
        Session session = toSession(result);
        AuthPrincipal principal = new AuthPrincipal(
                result.user().id(),
                result.organization().id(),
                result.role().name(),
                result.user().email(),
                result.user().name());
        String token = jwtService.issueToken(principal);
        return new AuthResponse(token, session);
    }

    private Session toSession(LoginResult result) {
        Organization org = organizationService.enrichOrganization(result.organization());
        return new Session(result.user(), org, result.role());
    }
}
