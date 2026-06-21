package jp.andpad.imart.monitoring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** フロー別モニタリング情報のファイル追記。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringFlowFileWriter {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private final IntraMartMonitoringProperties properties;

    public void append(String operation, List<MonitoringFlowData> models) {
        if (!properties.getFile().isEnabled() || models == null || models.isEmpty()) {
            return;
        }
        String pathValue = properties.getFile().getPath();
        if (pathValue == null || pathValue.isBlank()) {
            return;
        }
        Path path = Path.of(pathValue);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("--- ").append(TS.format(Instant.now())).append(" ").append(operation).append(" ---\n");
            for (MonitoringFlowData model : models) {
                sb.append("flowId: ").append(model.flowId()).append('\n');
                sb.append("flowName: ").append(model.flowName()).append('\n');
                sb.append("approveCount: ").append(model.approveCount()).append('\n');
                sb.append("approveEndCount: ").append(model.approveEndCount()).append('\n');
                sb.append("denyCount: ").append(model.denyCount()).append('\n');
                sb.append("discontinueCount: ").append(model.discontinueCount()).append('\n');
                sb.append("matterHandleCount: ").append(model.matterHandleCount()).append('\n');
                sb.append("averageTime: ").append(model.averageTime()).append('\n');
            }
            sb.append('\n');
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.warn("failed to append monitoring flow data to {}: {}", pathValue, ex.getMessage());
        }
    }
}
