package jp.andpad.imart.mail.spi;

import java.util.Optional;

import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.model.MailTemplateData;

/**
 * intra-mart メール送信 SPI。
 *
 * <p>IM SSJS API 相当:
 * <ul>
 *   <li>{@code MailTemplateManager} — テンプレート定義取得</li>
 *   <li>{@code MailTemplate.process} — テンプレート適用</li>
 *   <li>{@code MailSender.send} — 送信</li>
 * </ul>
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/MailTemplateManager/index.html">MailTemplateManager</a>
 */
public interface IntraMartMailService {

    /**
     * メールテンプレート定義を取得する。
     *
     * <p>IM API 相当: {@code MailTemplateManager.getMailTemplateDataWithLocale(mailId, localeId)}
     */
    Optional<MailTemplateData> getTemplate(String mailId, String localeId);

    /**
     * テンプレートを適用してメールを送信する。
     *
     * <p>IM API 相当: {@code MailTemplate.process} → {@code MailSender.send}
     */
    MailSendResult send(MailSendRequest request);
}
