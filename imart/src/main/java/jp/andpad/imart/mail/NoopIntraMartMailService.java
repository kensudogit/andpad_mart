package jp.andpad.imart.mail;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.model.MailTemplateData;
import jp.andpad.imart.mail.spi.IntraMartMailService;

/** メール通知無効時の no-op 実装。 */
@Service
@ConditionalOnProperty(name = "app.imart.mail.enabled", havingValue = "false")
public class NoopIntraMartMailService implements IntraMartMailService {

    @Override
    public Optional<MailTemplateData> getTemplate(String mailId, String localeId) {
        return Optional.empty();
    }

    @Override
    public MailSendResult send(MailSendRequest request) {
        return MailSendResult.fail("mail notifications are disabled");
    }
}
