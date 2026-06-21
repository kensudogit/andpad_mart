package jp.andpad.imart.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.stub.DevIntraMartMailService;

class DevIntraMartMailServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void sendAppliesTemplateParametersAndAppendsToFile() throws Exception {
        Path mailFile = tempDir.resolve("messages.log");
        IntraMartMailProperties properties = new IntraMartMailProperties();
        properties.setDefaultTo("test@example.com");
        properties.getFile().setEnabled(true);
        properties.getFile().setPath(mailFile.toString());

        DevIntraMartMailService service =
                new DevIntraMartMailService(properties, new MailMessageFileWriter(properties));

        MailSendResult result = service.send(new MailSendRequest(
                "dev-imart-session",
                "andpad-doc-submit",
                null,
                "ja",
                null,
                null,
                Map.of(
                        "title", "本館構造BIM",
                        "flowName", "書類承認",
                        "submitterName", "山田 太郎",
                        "action", "SUBMIT")));

        assertThat(result.sent()).isTrue();
        assertThat(result.subject()).contains("本館構造BIM");
        assertThat(result.body()).contains("山田 太郎");

        String saved = Files.readString(mailFile);
        assertThat(saved).contains("mailId: andpad-doc-submit");
        assertThat(saved).contains("subject: [ANDPAD] 資料承認申請: 本館構造BIM");
        assertThat(saved).contains("山田 太郎");
    }
}
