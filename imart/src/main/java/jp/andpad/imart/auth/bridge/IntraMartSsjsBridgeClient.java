package jp.andpad.imart.auth.bridge;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * intra-mart ランタイム上の SSJS API ブリッジへ HTTP で呼び出すクライアント。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class IntraMartSsjsBridgeClient {

    private final IntraMartAuthProperties properties;
    private final RestClient restClient;

    public SsjsInvokeResponse invoke(SsjsInvokeRequest request) {
        String baseUrl = properties.getBridge().getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return SsjsInvokeResponse.fail("app.imart.auth.bridge.base-url is not configured");
        }
        String url = baseUrl.replaceAll("/$", "") + properties.getBridge().getInvokePath();
        try {
            SsjsInvokeResponse response = restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(SsjsInvokeResponse.class);
            return response != null ? response : SsjsInvokeResponse.fail("empty response from IM bridge");
        } catch (RestClientException ex) {
            log.warn("IM SSJS bridge call failed: {}.{} — {}", request.api(), request.method(), ex.getMessage());
            return SsjsInvokeResponse.fail(ex.getMessage());
        }
    }
}
