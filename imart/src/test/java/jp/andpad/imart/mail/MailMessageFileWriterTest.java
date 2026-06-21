package jp.andpad.imart.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.andpad.imart.mail.model.MailSendRequest;

class MailMessageFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void appendWritesMailEntry() throws Exception {
        Path mailFile = tempDir.resolve("mail/messages.log");
        IntraMartMailProperties properties = new IntraMartMailProperties();
        properties.getFile().setEnabled(true);
        properties.getFile().setPath(mailFile.toString());

        MailMessageFileWriter writer = new MailMessageFileWriter(properties);
        writer.append(
                new MailSendRequest(
                        "session-1",
                        "andpad-doc-task",
                        null,
                        "ja",
                        List.of("user@example.com"),
                        List.of(),
                        Map.of("title", "安全書類")),
                List.of("user@example.com"),
                "[ANDPAD] 資料承認待ち: 安全書類",
                "承認をお願いします");

        String content = Files.readString(mailFile);
        assertThat(content).contains("mailId: andpad-doc-task");
        assertThat(content).contains("to: user@example.com");
        assertThat(content).contains("subject: [ANDPAD] 資料承認待ち: 安全書類");
        assertThat(content).contains("承認をお願いします");
    }
}
