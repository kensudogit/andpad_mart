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
 * intra-mart セッション認証 Servlet フィルター。
 *
 * <p>各 HTTP リクエストで IM セッション ID（Cookie またはヘッダー）を抽出し、
 * {@link IntraMartAuthorizationService} で検証後、
 * {@link IntraMartSecurityPrincipal} を Spring Security コンテキストへ設定する。
 *
 * <p>フィルター順序: {@code JwtAuthFilter} より前に実行され、IM セッション認証を優先する。
 * {@code app.imart.auth.enabled=true} のときのみ Bean 登録される。
 *
 * @see IntraMartAuthorizationService
 * @see jp.andpad.api.security.JwtAuthFilter
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
public class IntraMartAuthFilter extends OncePerRequestFilter {

    /** 認証設定（Cookie 名・ヘッダー名等）。 */
    private final IntraMartAuthProperties properties;

    /** 認可ファサード（セッション解決・Principal 生成）。 */
    private final IntraMartAuthorizationService authorizationService;

    /**
     * リクエストごとに IM セッション認証を実行する。
     *
     * <p>セッション ID が存在し有効な場合、SecurityContext に認証情報を設定する。
     * 無効な場合はログのみ出力し、後続フィルター（JWT 等）に処理を委譲する。
     */
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

    /**
     * リクエストから IM セッション ID を抽出する。
     *
     * <p>優先順位: 設定ヘッダー（{@code X-IM-Session-Id}） → 設定 Cookie（{@code imart-session}）
     *
     * @param request HTTP リクエスト
     * @return セッション ID、見つからない場合は {@code null}
     */
    private String extractSessionId(HttpServletRequest request) {
        String header = request.getHeader(properties.getSessionHeader());
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return extractCookie(request, properties.getSessionCookie());
    }

    /**
     * 指定名の Cookie 値を取得する。
     *
     * @param request HTTP リクエスト
     * @param name    Cookie 名
     * @return Cookie 値、存在しない場合は {@code null}
     */
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
