package jp.andpad.imart.mail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * intra-mart メール送信（{@code MailTemplateManager} / {@code MailTemplate}）設定。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/MailTemplateManager/index.html">MailTemplateManager</a>
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/platform/MailTemplate/index.html">MailTemplate</a>
 */
@Data
@ConfigurationProperties(prefix = "app.imart.mail")
public class IntraMartMailProperties {

    /** メール通知を有効化する。 */
    private boolean enabled = true;

    /** メールテンプレート適用ロケール ID。 */
    private String localeId = "ja";

    /** スタブモード等で送信先未指定時のデフォルト宛先。 */
    private String defaultTo = "dev@andpad.local";

    /** 送信メールのファイル追記設定。 */
    private final FileStore file = new FileStore();

    /** フロー ID ごとのメールテンプレート ID マッピング。 */
    private Map<String, WorkflowMailTemplates> workflow = defaultWorkflowTemplates();

    private static Map<String, WorkflowMailTemplates> defaultWorkflowTemplates() {
        WorkflowMailTemplates doc = new WorkflowMailTemplates();
        doc.setSubmitMailId("andpad-doc-submit");
        doc.setTaskMailId("andpad-doc-task");
        doc.setApprovedMailId("andpad-doc-approved");
        doc.setRejectedMailId("andpad-doc-rejected");
        doc.setReturnedMailId("andpad-doc-returned");
        Map<String, WorkflowMailTemplates> map = new LinkedHashMap<>();
        map.put("document-approval", doc);
        return map;
    }

    /** ワークフローイベント別メールテンプレート ID。 */
    @Data
    public static class WorkflowMailTemplates {
        private String submitMailId;
        private String taskMailId;
        private String approvedMailId;
        private String rejectedMailId;
        private String returnedMailId;
    }

    /** 送信メールのファイル出力設定。 */
    @Data
    public static class FileStore {
        /** ファイル追記を有効化する。 */
        private boolean enabled = true;

        /** 追記先ファイルパス（Railway 既定: {@code /app/data/mail/messages.log}）。 */
        private String path = "/app/data/mail/messages.log";
    }
}
