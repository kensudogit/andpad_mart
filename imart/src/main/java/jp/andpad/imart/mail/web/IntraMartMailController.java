package jp.andpad.imart.mail.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.imart.mail.IntraMartMailProperties;
import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.spi.IntraMartMailService;
import lombok.RequiredArgsConstructor;

/**
 * intra-mart メール送信 REST API（{@code MailTemplateManager} 連携）。
 */
@RestController
@RequestMapping("/auth/imart/mail")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.mail.enabled", havingValue = "true", matchIfMissing = true)
public class IntraMartMailController {

    private final IntraMartMailProperties properties;
    private final IntraMartMailService mailService;

    /**
     * メールテンプレート定義を取得する。
     *
     * <p>IM API 相当: {@code MailTemplateManager.getMailTemplateDataWithLocale}
     */
    @GetMapping("/templates/{mailId}")
    public ResponseEntity<Map<String, Object>> getTemplate(
            @org.springframework.web.bind.annotation.PathVariable String mailId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String localeId) {
        return mailService
                .getTemplate(mailId, localeId != null ? localeId : properties.getLocaleId())
                .map(data -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("mailId", data.mailId());
                    body.put("mailName", data.mailName());
                    body.put("localeId", data.localeId());
                    body.put("mailTemplatePath", data.mailTemplatePath());
                    body.put("subjectTemplate", data.subjectTemplate());
                    body.put("bodyTemplate", data.bodyTemplate());
                    body.put("defaultTo", data.defaultTo());
                    body.put("defaultCc", data.defaultCc());
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * テンプレートメールを送信する。
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestBody MailSendBody body) {
        MailSendResult result = mailService.send(new MailSendRequest(
                sessionHeader,
                body.mailId(),
                body.templatePath(),
                body.localeId() != null ? body.localeId() : properties.getLocaleId(),
                body.to(),
                body.cc(),
                body.parameters()));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sent", result.sent());
        response.put("subject", result.subject());
        response.put("body", result.body());
        response.put("error", result.error());
        return ResponseEntity.ok(response);
    }

    public record MailSendBody(
            String mailId,
            String templatePath,
            String localeId,
            List<String> to,
            List<String> cc,
            Map<String, String> parameters) {}
}
