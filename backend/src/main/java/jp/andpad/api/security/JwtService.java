package jp.andpad.api.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jp.andpad.api.config.AppProperties;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppProperties appProperties;

    public String issueToken(AuthPrincipal principal) {
        Instant now = Instant.now();
        Instant expires = now.plus(appProperties.jwt().ttlHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .claim("uid", principal.userId())
                .claim("oid", principal.orgId())
                .claim("role", principal.role())
                .claim("email", principal.email())
                .claim("name", principal.name())
                .subject(principal.userId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expires))
                .signWith(signingKey())
                .compact();
    }

    public AuthPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthPrincipal(
                claims.get("uid", String.class),
                claims.get("oid", String.class),
                claims.get("role", String.class),
                claims.get("email", String.class),
                claims.get("name", String.class));
    }

    private SecretKey signingKey() {
        byte[] secret = appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secret, "HmacSHA256");
    }
}
