package jp.andpad.api.auth;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.andpad.api.auth.dto.AuthResponse;
import jp.andpad.api.auth.dto.LoginRequest;
import jp.andpad.api.auth.dto.RegisterRequest;
import jp.andpad.api.config.AppProperties;
import jp.andpad.api.repository.AuthRepository.RegisterInput;
import jp.andpad.api.service.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppProperties appProperties;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
            HttpServletResponse response) {
        try {
            AuthResponse payload = authService.login(request.email(), request.password());
            setTokenCookie(httpRequest, response, payload.token());
            return ResponseEntity.ok(payload);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest,
            HttpServletResponse response) {
        try {
            AuthResponse payload = authService.register(new RegisterInput(
                    request.clinicName(), request.slug(), request.ownerName(),
                    request.email(), request.password()));
            setTokenCookie(httpRequest, response, payload.token());
            return ResponseEntity.ok(payload);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout(HttpServletRequest request, HttpServletResponse response) {
        clearTokenCookie(request, response);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private void setTokenCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("dv_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure(request));
        cookie.setMaxAge((int) Duration.ofHours(appProperties.jwt().ttlHours()).toSeconds());
        response.addCookie(cookie);
    }

    private void clearTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("dv_token", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure(request));
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private static boolean cookieSecure(HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }
        return "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    }
}
