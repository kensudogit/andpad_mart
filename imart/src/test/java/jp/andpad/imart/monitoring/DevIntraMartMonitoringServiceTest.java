package jp.andpad.imart.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.MonitoringFlowDataSearchCondition;
import jp.andpad.imart.monitoring.stub.DevIntraMartMonitoringService;
import jp.andpad.imart.monitoring.stub.InMemoryMonitoringFlowStore;
import jp.andpad.imart.monitoring.NoopMonitoringFlowRecorder;
import jp.andpad.imart.monitoring.MonitoringFlowPersister;

class DevIntraMartMonitoringServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void createAndListMonitoringFlowData() throws Exception {
        Path logFile = tempDir.resolve("flow-data.log");
        IntraMartMonitoringProperties properties = new IntraMartMonitoringProperties();
        properties.getFile().setEnabled(true);
        properties.getFile().setPath(logFile.toString());

        InMemoryMonitoringFlowStore store = new InMemoryMonitoringFlowStore();
        MonitoringFlowPersister persister = new MonitoringFlowPersister(
                properties, new NoopMonitoringFlowRecorder());
        DevIntraMartMonitoringService service =
                new DevIntraMartMonitoringService(properties, store, new MonitoringFlowFileWriter(properties), persister);

        var created = service.createMonitoringFlowData(
                "dev-imart-session",
                List.of(new MonitoringFlowData(
                        "document-approval",
                        "書類承認",
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        "5",
                        "5",
                        "5",
                        "5",
                        null)));

        assertThat(created.success()).isTrue();

        var listed = service.getMonitoringFlowDataList(
                "dev-imart-session", new MonitoringFlowDataSearchCondition(List.of("document-approval")));
        assertThat(listed.success()).isTrue();
        assertThat(listed.data()).hasSize(1);
        assertThat(listed.data().get(0).flowId()).isEqualTo("document-approval");
        assertThat(listed.data().get(0).approveEndCount()).isEqualTo("1");

        String saved = Files.readString(logFile);
        assertThat(saved).contains("createMonitoringFlowData");
        assertThat(saved).contains("document-approval");
    }
}
