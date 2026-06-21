package jp.andpad.api.mail;

import org.springframework.stereotype.Component;

import jp.andpad.api.repository.MailMessageRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.mail.model.SentMailSnapshot;
import jp.andpad.imart.mail.spi.SentMailRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 送信メールを PostgreSQL に保存する。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresSentMailRecorder implements SentMailRecorder {

    private final MailMessageRepository mailMessageRepository;

    @Override
    public void record(SentMailSnapshot snapshot) {
        try {
            String orgId = TenantContext.orgId();
            if (orgId == null) {
                log.warn("mail message not stored: organization context missing");
                return;
            }
            mailMessageRepository.insert(orgId, snapshot);
        } catch (Exception ex) {
            log.warn("failed to store mail message: {}", ex.getMessage());
        }
    }
}
