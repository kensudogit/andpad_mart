package jp.andpad.imart.mail.model;

import java.util.List;
import java.util.Map;

/**
 * 送信済みメールの永続化用スナップショット。
 */
public record SentMailSnapshot(
        String mailId,
        String localeId,
        List<String> to,
        List<String> cc,
        String subject,
        String body,
        Map<String, String> parameters,
        String entityType,
        String entityId,
        String flowId,
        boolean sent) {

    public static SentMailSnapshot of(
            MailSendRequest request, List<String> to, String subject, String body, boolean sent) {
        Map<String, String> params = request.parameters();
        return new SentMailSnapshot(
                request.mailId(),
                request.localeId(),
                to,
                request.cc(),
                subject,
                body,
                params,
                params.get("entityType"),
                params.get("entityId"),
                params.get("flowId"),
                sent);
    }
}
