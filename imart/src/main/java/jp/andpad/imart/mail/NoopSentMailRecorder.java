package jp.andpad.imart.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import jp.andpad.imart.mail.model.SentMailSnapshot;
import jp.andpad.imart.mail.spi.SentMailRecorder;

/** DB 永続化未設定時の no-op 実装。 */
@Component
@ConditionalOnMissingBean(SentMailRecorder.class)
public class NoopSentMailRecorder implements SentMailRecorder {

    @Override
    public void record(SentMailSnapshot snapshot) {
        // no-op
    }
}
