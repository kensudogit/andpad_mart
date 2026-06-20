package jp.andpad.imart.auth.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestClient;

import jp.andpad.imart.auth.IntraMartAuthProperties;

/**
 * intra-mart 認証・認可の自動設定。
 */
@AutoConfiguration
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@EnableConfigurationProperties(IntraMartAuthProperties.class)
@ComponentScan(basePackages = "jp.andpad.imart.auth")
public class IntraMartAuthAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
    RestClient intraMartRestClient(IntraMartAuthProperties properties) {
        var requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(
                Duration.ofSeconds(properties.getBridge().getConnectTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getBridge().getReadTimeoutSeconds()));
        return RestClient.builder().requestFactory(requestFactory).build();
    }
}
