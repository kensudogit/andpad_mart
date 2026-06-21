package jp.andpad.imart.mail.model;

/**
 * メール送信結果。
 */
public record MailSendResult(boolean sent, String subject, String body, String error) {

    public static MailSendResult ok(String subject, String body) {
        return new MailSendResult(true, subject, body, null);
    }

    public static MailSendResult fail(String error) {
        return new MailSendResult(false, null, null, error);
    }
}
