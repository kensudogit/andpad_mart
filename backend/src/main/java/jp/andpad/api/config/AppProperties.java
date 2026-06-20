package jp.andpad.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt) {

    public record Jwt(String secret, int ttlHours) {}
}
