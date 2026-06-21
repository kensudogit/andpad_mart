package jp.andpad.imart.mail.model;

import java.util.List;

/**
 * {@code MailTemplateManager.getMailTemplateDataWithLocale} 相当のテンプレート情報。
 */
public record MailTemplateData(
        String mailId,
        String mailName,
        String localeId,
        String mailTemplatePath,
        String subjectTemplate,
        String bodyTemplate,
        List<String> defaultTo,
        List<String> defaultCc) {}
