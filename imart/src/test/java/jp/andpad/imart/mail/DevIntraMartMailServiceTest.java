package jp.andpad.imart.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import jp.andpad.imart.mail.model.MailSendRequest;
import jp.andpad.imart.mail.model.MailSendResult;
import jp.andpad.imart.mail.stub.DevIntraMartMailService;

class DevIntraMartMailServiceTest {

    @Test
    void sendAppliesTemplateParameters() {
        IntraMartMailProperties properties = new IntraMartMailProperties();
        properties.setDefaultTo("test@example.com");
        DevIntraMartMailService service = new DevIntraMartMailService(properties);

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
    }
}
