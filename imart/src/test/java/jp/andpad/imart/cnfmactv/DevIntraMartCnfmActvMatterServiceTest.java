package jp.andpad.imart.cnfmactv.stub;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.andpad.imart.cnfmactv.CnfmActvMatterFileWriter;
import jp.andpad.imart.cnfmactv.CnfmActvMatterPersister;
import jp.andpad.imart.cnfmactv.IntraMartCnfmActvMatterProperties;
import jp.andpad.imart.cnfmactv.NoopCnfmActvMatterRecorder;
import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.model.CnfmActvMatterSearchCondition;

class DevIntraMartCnfmActvMatterServiceTest {

    @Test
    void registerAndListCnfmActvMatters(@TempDir Path tempDir) throws Exception {
        IntraMartCnfmActvMatterProperties properties = new IntraMartCnfmActvMatterProperties();
        properties.getFile().setPath(tempDir.resolve("cnfm.log").toString());
        InMemoryCnfmActvMatterStore store = new InMemoryCnfmActvMatterStore();
        CnfmActvMatterFileWriter fileWriter = new CnfmActvMatterFileWriter(properties);
        CnfmActvMatterPersister persister =
                new CnfmActvMatterPersister(properties, new NoopCnfmActvMatterRecorder());
        DevIntraMartCnfmActvMatterService service =
                new DevIntraMartCnfmActvMatterService(store, fileWriter, persister);

        service.register(new ActvMatterCnfmData(
                "wf-doc-001",
                "document-approval",
                "資料承認フロー",
                "安全書類提出",
                "MAT-001",
                "reviewer_approval",
                "user_demo",
                "山田 太郎",
                "2026/06/08 09:00:00",
                "2026/06/08 09:30:00",
                "0",
                "1",
                ActvMatterCnfmData.LIST_CONFIRM));
        service.register(new ActvMatterCnfmData(
                "wf-doc-pending",
                "document-approval",
                "資料承認フロー",
                "図面承認申請",
                "MAT-2026-042",
                "reviewer_approval",
                "user_demo",
                "佐藤 花子",
                "2026/06/08 10:00:00",
                "2026/06/08 10:15:00",
                "0",
                "2",
                ActvMatterCnfmData.LIST_LUMP_CONFIRM));

        var confirmList = service.getCnfmList(
                "dev-imart-session", new CnfmActvMatterSearchCondition(List.of("document-approval"), null));
        assertThat(confirmList.success()).isTrue();
        assertThat(confirmList.data()).hasSize(1);
        assertThat(confirmList.data().get(0).matterName()).isEqualTo("安全書類提出");

        var lumpList = service.getLumpCnfmList(
                "dev-imart-session", false, new CnfmActvMatterSearchCondition(List.of("document-approval"), null));
        assertThat(lumpList.success()).isTrue();
        assertThat(lumpList.data()).hasSize(1);
        assertThat(lumpList.data().get(0).matterName()).isEqualTo("図面承認申請");

        String saved = Files.readString(tempDir.resolve("cnfm.log"));
        assertThat(saved).contains("register");
    }
}
