package jp.andpad.imart.stamp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.andpad.imart.stamp.model.StampListSearchCondition;
import jp.andpad.imart.stamp.stub.DevIntraMartStampService;
import jp.andpad.imart.stamp.stub.InMemoryMatterStampStore;

class DevIntraMartStampServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void registerAndListStamps() throws Exception {
        Path logFile = tempDir.resolve("matter-stamps.log");
        IntraMartStampProperties properties = new IntraMartStampProperties();
        properties.getFile().setEnabled(true);
        properties.getFile().setPath(logFile.toString());

        InMemoryMatterStampStore store = new InMemoryMatterStampStore();
        MatterStampPersister persister = new MatterStampPersister(properties, new NoopMatterStampRecorder());
        DevIntraMartStampService service = new DevIntraMartStampService(
                store, new MatterStampFileWriter(properties), persister);

        service.registerStamp(new jp.andpad.imart.stamp.model.MatterStampData(
                "im-matter-1",
                "1",
                "final",
                "2026/06/08 10:00:00",
                "APPROVE",
                "山田 太郎",
                "user",
                "最終承認",
                "node",
                "approveEnd",
                "type",
                "approveEnd",
                "0",
                "document-approval",
                "DOCUMENT",
                "rec-1",
                "wf-1"));

        var listed = service.getStampList("dev-imart-session", "im-matter-1", StampListSearchCondition.empty());
        assertThat(listed.success()).isTrue();
        assertThat(listed.stamps()).hasSize(1);
        assertThat(listed.stamps().get(0).stampType()).isEqualTo("approveEnd");

        var count = service.getStampListCount("dev-imart-session", "im-matter-1", StampListSearchCondition.empty());
        assertThat(count.success()).isTrue();
        assertThat(count.count()).isEqualTo(1);

        String saved = Files.readString(logFile);
        assertThat(saved).contains("registerStamp");
        assertThat(saved).contains("im-matter-1");
    }
}
