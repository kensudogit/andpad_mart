package jp.andpad.imart.mail;

import java.util.List;

import org.springframework.stereotype.Component;

import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.SentMailSnapshot;
import jp.andpad.imart.mail.spi.SentMailRecorder;
import lombok.RequiredArgsConstructor;

/** 送信メールをファイルおよび永続化 SPI へ保存する。 */
@Component
@RequiredArgsConstructor
public class MailMessagePersister {

    private final IntraMartMailProperties properties;
    private final MailMessageFileWriter fileWriter;
    private final SentMailRecorder sentMailRecorder;

    /**
     * 送信メールを保存する。
     *
     * @param request 送信リクエスト
     * @param to      宛先
     * @param subject 件名
     * @param body    本文
     * @param sent    送信成功フラグ
     */
    public void persist(MailSendRequest request, List<String> to, String subject, String body, boolean sent) {
        if (sent && properties.getFile().isEnabled()) {
            fileWriter.append(request, to, subject, body);
        }
        if (properties.isStoreInDatabase()) {
            sentMailRecorder.record(SentMailSnapshot.of(request, to, subject, body, sent));
        }
    }
}
