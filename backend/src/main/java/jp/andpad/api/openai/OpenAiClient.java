package jp.andpad.api.openai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

/** OpenAI Chat Completions（建設 PM 向け AI 相談・分析）。 */
@Component
@Slf4j
public class OpenAiClient {

    public static final String CONSTRUCTION_CONSULT_SYSTEM =
            """
            You are an AI assistant for construction project management professionals in Japan (ANDPAD-style platform).
            Help with site safety, schedule coordination, subcontractor management, quality inspection, document workflows, BIM/digital delivery, and general construction PM best practices.
            Be practical and concise. When unsure, say what information is needed and suggest checking site rules or consulting a qualified supervisor.
            Do not provide legally binding engineering sign-off. Respond in Japanese unless the user writes in another language.""";

    public static final String CONSTRUCTION_ANALYTICS_SYSTEM =
            """
            You are a construction project management analyst (AI Board). Given JSON analytics KPIs for a construction PM platform, respond ONLY with valid JSON:
            {"summary":"...","strengths":["..."],"risks":["..."],"recommendations":["..."]}
            Write in Japanese. Focus on project delivery, schedule risk, module adoption, billing trends, safety/compliance awareness, and actionable site management advice.""";

    private final RestClient http;
    private final String apiKey;
    private final String model;

    public OpenAiClient(OpenAiProperties properties) {
        this.apiKey = properties.apiKey() == null ? "" : properties.apiKey().trim();
        this.model = StringUtils.hasText(properties.model()) ? properties.model().trim() : "gpt-4o-mini";
        this.http = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public boolean isEnabled() {
        return StringUtils.hasText(apiKey);
    }

    public String chat(String systemPrompt, List<OpenAiChatMessage> history, String userMessage) {
        if (!isEnabled()) {
            throw new IllegalStateException("OPENAI_API_KEY is not configured");
        }
        List<OpenAiChatMessage> messages = new ArrayList<>();
        messages.add(new OpenAiChatMessage("system", systemPrompt));
        if (history != null) {
            for (OpenAiChatMessage msg : history) {
                if (msg != null && StringUtils.hasText(msg.content())) {
                    messages.add(msg);
                }
            }
        }
        messages.add(new OpenAiChatMessage("user", userMessage));

        try {
            OpenAiChatResponse response = http.post()
                    .uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(new OpenAiChatRequest(model, messages))
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            if (response == null) {
                throw new IllegalStateException("openai: empty response");
            }
            if (response.error() != null && StringUtils.hasText(response.error().message())) {
                throw new IllegalStateException("openai: " + response.error().message());
            }
            if (response.choices() == null || response.choices().isEmpty()) {
                throw new IllegalStateException("openai: no choices");
            }
            OpenAiChatMessage answer = response.choices().getFirst().message();
            if (answer == null || !StringUtils.hasText(answer.content())) {
                throw new IllegalStateException("openai: empty message");
            }
            return answer.content().trim();
        } catch (RestClientResponseException ex) {
            log.warn("OpenAI HTTP {}: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException(
                    "openai http " + ex.getStatusCode().value() + ": " + truncate(ex.getResponseBodyAsString(), 120));
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("OpenAI chat failed: {}", ex.getMessage());
            throw new IllegalStateException(truncate(ex.getMessage(), 120), ex);
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
