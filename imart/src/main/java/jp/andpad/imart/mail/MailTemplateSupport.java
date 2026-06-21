package jp.andpad.imart.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.andpad.imart.mail.model.MailTemplateData;

/** メールテンプレート適用ユーティリティ。 */
public final class MailTemplateSupport {

    private MailTemplateSupport() {}

    public static String applyParameters(String template, Map<String, String> parameters) {
        if (template == null) {
            return "";
        }
        String result = template;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static MailTemplateData mapTemplateData(String mailId, String localeId, Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        Map<String, Object> fileData = raw.get("mailTempFileData") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        return new MailTemplateData(
                stringValue(raw.get("mailId"), mailId),
                stringValue(raw.get("mailName"), mailId),
                stringValue(raw.get("localeId"), localeId),
                stringValue(raw.get("mailTemplatePath"), null),
                stringValue(fileData.get("mailTemplateSubject"), null),
                stringValue(fileData.get("mailTemplateBody"), null),
                stringList(fileData.get("mailTemplateTo")),
                stringList(fileData.get("mailTemplateCc")));
    }

    @SuppressWarnings("unchecked")
    public static MailTemplateData mapProcessedMail(Map<String, Object> processed) {
        if (processed == null) {
            return null;
        }
        Map<String, Object> body = processed.get("body") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        return new MailTemplateData(
                null,
                null,
                null,
                null,
                stringValue(processed.get("subject"), null),
                stringValue(body.get("text"), stringValue(processed.get("text"), null)),
                stringList(processed.get("to")),
                stringList(processed.get("cc")));
    }

    private static String stringValue(Object value, String fallback) {
        return value != null ? value.toString() : fallback;
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object address = map.get("address");
                if (address != null) {
                    result.add(address.toString());
                }
            } else if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }
}
