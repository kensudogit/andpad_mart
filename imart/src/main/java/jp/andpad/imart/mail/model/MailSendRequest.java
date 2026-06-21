package jp.andpad.imart.mail.model;

import java.util.List;
import java.util.Map;

/**
 * テンプレートメール送信リクエスト。
 *
 * @param sessionId      IM セッション ID（HTTP ブリッジのユーザコンテキスト復元用）
 * @param mailId         IM メール ID（{@code MailTemplateManager} 管理）
 * @param templatePath   メールテンプレートパス（mailId 未指定時に直接指定）
 * @param localeId       ロケール ID
 * @param to             送信先（未指定時はテンプレート既定値）
 * @param cc             CC（任意）
 * @param parameters     テンプレート置換パラメータ
 */
public record MailSendRequest(
        String sessionId,
        String mailId,
        String templatePath,
        String localeId,
        List<String> to,
        List<String> cc,
        Map<String, String> parameters) {

    public MailSendRequest {
        parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        to = to != null ? List.copyOf(to) : List.of();
        cc = cc != null ? List.copyOf(cc) : List.of();
    }
}
