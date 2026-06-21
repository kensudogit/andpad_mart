package jp.andpad.imart.mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jp.andpad.imart.mail.model.MailSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 送信メール本文をファイルへ追記保存する。
 *
 * <p>Railway 統合デプロイではデフォルト {@code /app/data/mail/messages.log} に出力する。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailMessageFileWriter {

    private final IntraMartMailProperties properties;

    /**
     * 送信メールをファイル末尾に追記する。
     *
     * @param request 送信リクエスト
     * @param to      宛先
     * @param subject 件名
     * @param body    本文
     */
    public void append(MailSendRequest request, List<String> to, String subject, String body) {
        if (!properties.getFile().isEnabled()) {
            return;
        }
        try {
            Path path = Path.of(properties.getFile().getPath());
            Files.createDirectories(path.getParent());
            String entry = formatEntry(request, to, subject, body);
            Files.writeString(
                    path,
                    entry,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.warn("failed to append mail message to {}: {}", properties.getFile().getPath(), ex.getMessage());
        }
    }

    private static String formatEntry(
            MailSendRequest request, List<String> to, String subject, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(Instant.now()).append(" ===\n");
        sb.append("mailId: ").append(nullToDash(request.mailId())).append('\n');
        sb.append("locale: ").append(nullToDash(request.localeId())).append('\n');
        sb.append("to: ").append(String.join(", ", to)).append('\n');
        if (!request.cc().isEmpty()) {
            sb.append("cc: ").append(String.join(", ", request.cc())).append('\n');
        }
        sb.append("subject: ").append(subject).append('\n');
        sb.append("body:\n").append(body).append('\n');
        if (!request.parameters().isEmpty()) {
            sb.append("parameters:\n");
            for (Map.Entry<String, String> entry : request.parameters().entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
            }
        }
        sb.append("---\n");
        return sb.toString();
    }

    private static String nullToDash(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }
}
