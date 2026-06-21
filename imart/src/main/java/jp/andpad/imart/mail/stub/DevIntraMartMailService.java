package jp.andpad.imart.mail.stub;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.mail.IntraMartMailProperties;
import jp.andpad.imart.mail.MailMessageFileWriter;
import jp.andpad.imart.mail.MailTemplateSupport;
import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.model.MailTemplateData;
import jp.andpad.imart.mail.spi.IntraMartMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ローカル開発用 {@code MailTemplateManager} / {@code MailTemplate} スタブ実装。
 *
 * <p>IM サーバー無しでワークフロー通知メールの内容をログ出力し、ファイルへ追記する。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.mail.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartMailService implements IntraMartMailService {

    private final IntraMartMailProperties properties;
    private final MailMessageFileWriter fileWriter;

    @Override
    public Optional<MailTemplateData> getTemplate(String mailId, String localeId) {
        return Optional.of(stubTemplate(mailId, localeId));
    }

    @Override
    public MailSendResult send(MailSendRequest request) {
        Optional<MailTemplateData> template = resolveTemplate(request);
        if (template.isEmpty()) {
            return MailSendResult.fail("mail template not found: " + request.mailId());
        }
        MailTemplateData data = template.get();
        String subject = MailTemplateSupport.applyParameters(data.subjectTemplate(), request.parameters());
        String body = MailTemplateSupport.applyParameters(data.bodyTemplate(), request.parameters());
        List<String> to = request.to().isEmpty()
                ? (data.defaultTo().isEmpty() ? List.of(properties.getDefaultTo()) : data.defaultTo())
                : request.to();
        log.info(
                "[stub-mail] sent mailId={} locale={} to={} subject={} body={}",
                request.mailId(),
                request.localeId(),
                to,
                subject,
                body.replace('\n', ' '));
        fileWriter.append(request, to, subject, body);
        return MailSendResult.ok(subject, body);
    }

    private Optional<MailTemplateData> resolveTemplate(MailSendRequest request) {
        if (request.mailId() != null && !request.mailId().isBlank()) {
            return getTemplate(request.mailId(), request.localeId());
        }
        if (request.templatePath() != null && !request.templatePath().isBlank()) {
            return Optional.of(new MailTemplateData(
                    null,
                    request.templatePath(),
                    request.localeId(),
                    request.templatePath(),
                    "ANDPAD Notification",
                    "Template path: ${templatePath}",
                    List.of(properties.getDefaultTo()),
                    List.of()));
        }
        return Optional.empty();
    }

    private MailTemplateData stubTemplate(String mailId, String localeId) {
        Map<String, String> defaults = stubDefaults();
        String subject = defaults.getOrDefault(mailId + ".subject", "[ANDPAD] ${title}");
        String body = defaults.getOrDefault(
                mailId + ".body",
                """
                ${submitterName} 様

                資料「${title}」のワークフローが更新されました。
                フロー: ${flowName}
                ステップ: ${stepName}
                操作: ${action}

                ${comment}
                """);
        return new MailTemplateData(
                mailId,
                mailId,
                localeId,
                "mail_template/andpad/" + mailId + ".xml",
                subject,
                body,
                List.of(properties.getDefaultTo()),
                List.of());
    }

    private static Map<String, String> stubDefaults() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("andpad-doc-submit.subject", "[ANDPAD] 資料承認申請: ${title}");
        map.put("andpad-doc-task.subject", "[ANDPAD] 資料承認待ち: ${title}");
        map.put("andpad-doc-approved.subject", "[ANDPAD] 資料承認完了: ${title}");
        map.put("andpad-doc-rejected.subject", "[ANDPAD] 資料却下: ${title}");
        map.put("andpad-doc-returned.subject", "[ANDPAD] 資料差戻し: ${title}");
        return map;
    }
}
