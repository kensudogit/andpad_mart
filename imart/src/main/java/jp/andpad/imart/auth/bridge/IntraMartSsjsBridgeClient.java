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
 * intra-mart SSJS API ブリッジ HTTP クライアント。
 *
 * <p>Spring Boot（スタンドアロンまたは WAR 外部）から intra-mart ランタイム上の
 * SSJS 呼び出しエンドポイントへ {@link SsjsInvokeRequest} を POST し、
 * {@link SsjsInvokeResponse} を受け取る。
 *
 * <p>{@code app.imart.auth.mode=http} かつ {@code app.imart.auth.enabled=true} のときのみ有効。
 *
 * @see IntraMartAuthProperties.Bridge
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class IntraMartSsjsBridgeClient {

    /** 認証・認可設定（ブリッジ URL・タイムアウト等）。 */
    private final IntraMartAuthProperties properties;

    /** HTTP 通信クライアント。 */
    private final RestClient restClient;

    /**
     * SSJS API ブリッジを呼び出す。
     *
     * <p>呼び出し URL: {@code bridge.baseUrl + bridge.invokePath}
     *
     * @param request SSJS 呼び出しリクエスト
     * @return 呼び出し結果（通信エラー時は {@code success=false}）
     */
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
