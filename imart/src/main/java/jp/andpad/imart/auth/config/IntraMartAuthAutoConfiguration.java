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
 * intra-mart 認証・認可サブシステムの Spring Boot 自動設定。
 *
 * <p>{@code app.imart.auth.enabled=true} のときのみ有効化され、
 * {@code jp.andpad.imart.auth} パッケージ以下の Bean（フィルター、SPI 実装、
 * REST コントローラ等）をスキャン・登録する。
 *
 * <p>メインの {@link jp.andpad.imart.config.IntraMartAutoConfiguration} とは
 * 独立して読み込まれ、認証無効時は auth パッケージの Bean が一切登録されない。
 *
 * @see IntraMartAuthProperties
 * @see jp.andpad.imart.auth.security.IntraMartAuthFilter
 */
@AutoConfiguration
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@EnableConfigurationProperties(IntraMartAuthProperties.class)
@ComponentScan(basePackages = "jp.andpad.imart.auth")
public class IntraMartAuthAutoConfiguration {

    /**
     * intra-mart SSJS ブリッジ呼び出し用 {@link RestClient} Bean。
     *
     * <p>{@code app.imart.auth.mode=http} のときのみ登録。
     * 接続・読み取りタイムアウトは {@link IntraMartAuthProperties.Bridge} から取得する。
     *
     * @param properties 認証設定
     * @return タイムアウト設定済み RestClient
     */
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
