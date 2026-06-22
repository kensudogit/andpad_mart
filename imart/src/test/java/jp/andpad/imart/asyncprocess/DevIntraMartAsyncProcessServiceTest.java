package jp.andpad.imart.asyncprocess.stub;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.andpad.imart.asyncprocess.AsyncProcessFileWriter;
import jp.andpad.imart.asyncprocess.AsyncProcessPersister;
import jp.andpad.imart.asyncprocess.IntraMartAsyncProcessProperties;
import jp.andpad.imart.asyncprocess.NoopAsyncProcessRecorder;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;

class DevIntraMartAsyncProcessServiceTest {

    @Test
    void createAndListAsyncProcessStatus(@TempDir Path tempDir) throws Exception {
        IntraMartAsyncProcessProperties properties = new IntraMartAsyncProcessProperties();
        properties.getFile().setPath(tempDir.resolve("async.log").toString());
        InMemoryAsyncProcessStore store = new InMemoryAsyncProcessStore();
        AsyncProcessFileWriter fileWriter = new AsyncProcessFileWriter(properties);
        AsyncProcessPersister persister =
                new AsyncProcessPersister(properties, new NoopAsyncProcessRecorder());
        DevIntraMartAsyncProcessService service =
                new DevIntraMartAsyncProcessService(properties, store, fileWriter, persister);

        var created = service.createAsyncProcessStatusData(
                "dev-imart-session",
                List.of(new AsyncProcessStatusData(
                        "accept-test-1",
                        "2",
                        "user_demo",
                        "user_demo",
                        "document-approval",
                        "安全書類提出",
                        "MAT-001",
                        "処理中",
                        "reviewer_approval",
                        null,
                        "2026/06/08 10:00:00",
                        "SUBMIT",
                        "queue-test-1",
                        null,
                        "wf-doc-001")));

        assertThat(created.success()).isTrue();
        assertThat(created.data()).hasSize(1);

        var listed = service.getAsyncProcessStatusDataList(
                "dev-imart-session",
                new AsyncProcessStatusSearchCondition(List.of("document-approval"), List.of(), null));
        assertThat(listed.success()).isTrue();
        assertThat(listed.data()).hasSize(1);
        assertThat(listed.data().get(0).matterName()).isEqualTo("安全書類提出");

        String saved = Files.readString(tempDir.resolve("async.log"));
        assertThat(saved).contains("createAsyncProcessStatusData");
    }
}
