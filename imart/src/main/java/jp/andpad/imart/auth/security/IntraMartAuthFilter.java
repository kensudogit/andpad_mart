package jp.andpad.imart.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.IntraMartAuthorizationService;
import jp.andpad.imart.auth.IntraMartSecurityPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * intra-mart セッション Cookie / ヘッダーを検証し Spring Security コンテキストへ設定するフィルター。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class IntraMartAuthFilter extends OncePerRequestFilter {

    private final IntraMartAuthProperties properties;
    private final IntraMartAuthorizationService authorizationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String sessionId = extractSessionId(request);
        if (sessionId != null) {
            try {
                IntraMartSecurityPrincipal principal = authorizationService.resolvePrincipal(sessionId);
                var authentication = new UsernamePasswordAuthenticationToken(principal, sessionId, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                log.debug("IM session auth failed: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractSessionId(HttpServletRequest request) {
        String header = request.getHeader(properties.getSessionHeader());
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return extractCookie(request, properties.getSessionCookie());
    }

    private static String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
