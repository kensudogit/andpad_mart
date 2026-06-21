package jp.andpad.imart.mail.http;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import jp.andpad.imart.mail.IntraMartMailProperties;
import jp.andpad.imart.mail.MailMessagePersister;
import jp.andpad.imart.mail.MailTemplateSupport;
import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.model.MailTemplateData;
import jp.andpad.imart.mail.spi.IntraMartMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP ブリッジ経由の {@code MailTemplateManager} / {@code MailTemplate} / {@code MailSender} 実装。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.mail.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartMailService implements IntraMartMailService {

    private final IntraMartMailProperties properties;
    private final IntraMartSsjsBridgeClient bridgeClient;
    private final MailMessagePersister persister;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MailTemplateData> getTemplate(String mailId, String localeId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "im_workflow",
                "MailTemplateManager",
                "getMailTemplateDataWithLocale",
                List.of(mailId, localeId),
                null,
                Map.of()));
        if (!response.success()) {
            log.warn("MailTemplateManager.getMailTemplateDataWithLocale failed: {}", response.error());
            return Optional.empty();
        }
        Map<String, Object> payload = unwrapWorkflowResult(response.result());
        if (payload == null) {
            return Optional.empty();
        }
        return Optional.of(MailTemplateSupport.mapTemplateData(mailId, localeId, payload));
    }

    @Override
    public MailSendResult send(MailSendRequest request) {
        String localeId = request.localeId() != null ? request.localeId() : properties.getLocaleId();
        String templatePath = request.templatePath();
        MailTemplateData templateData = null;

        if ((templatePath == null || templatePath.isBlank()) && request.mailId() != null) {
            Optional<MailTemplateData> loaded = getTemplate(request.mailId(), localeId);
            if (loaded.isEmpty()) {
                return MailSendResult.fail("mail template not found: " + request.mailId());
            }
            templateData = loaded.get();
            templatePath = templateData.mailTemplatePath();
        }

        if (templatePath == null || templatePath.isBlank()) {
            return MailSendResult.fail("mail template path is not configured");
        }

        SsjsInvokeResponse processed = bridgeClient.invoke(new SsjsInvokeRequest(
                "platform",
                "MailTemplate",
                "process",
                List.of(templatePath, localeId, request.parameters()),
                request.sessionId(),
                Map.of()));
        if (!processed.success()) {
            return MailSendResult.fail("MailTemplate.process failed: " + processed.error());
        }

        MailTemplateData rendered = MailTemplateSupport.mapProcessedMail(asMap(processed.result()));
        if (rendered == null && templateData != null) {
            rendered = new MailTemplateData(
                    request.mailId(),
                    templateData.mailName(),
                    localeId,
                    templatePath,
                    MailTemplateSupport.applyParameters(templateData.subjectTemplate(), request.parameters()),
                    MailTemplateSupport.applyParameters(templateData.bodyTemplate(), request.parameters()),
                    templateData.defaultTo(),
                    templateData.defaultCc());
        }
        if (rendered == null) {
            return MailSendResult.fail("failed to render mail template");
        }

        List<String> to = request.to().isEmpty() ? rendered.defaultTo() : request.to();
        if (to.isEmpty()) {
            to = List.of(properties.getDefaultTo());
        }

        Map<String, Object> sendPayload = new LinkedHashMap<>();
        sendPayload.put("to", to);
        sendPayload.put("cc", request.cc().isEmpty() ? rendered.defaultCc() : request.cc());
        sendPayload.put("subject", rendered.subjectTemplate());
        sendPayload.put("body", rendered.bodyTemplate());
        sendPayload.put("locale", localeId);

        SsjsInvokeResponse sent = bridgeClient.invoke(new SsjsInvokeRequest(
                "platform",
                "MailSender",
                "send",
                List.of(sendPayload),
                request.sessionId(),
                Map.of("operation", "sendPreparedMail")));
        if (!sent.success()) {
            return MailSendResult.fail("MailSender.send failed: " + sent.error());
        }
        persister.persist(request, to, rendered.subjectTemplate(), rendered.bodyTemplate(), true);
        return MailSendResult.ok(rendered.subjectTemplate(), rendered.bodyTemplate());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> unwrapWorkflowResult(Object result) {
        if (result instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return (Map<String, Object>) dataMap;
            }
            return (Map<String, Object>) map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object result) {
        if (result instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }
}
