package jp.andpad.imart.stamp;

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

import jp.andpad.imart.stamp.model.MatterStampData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 印影データのファイル追記。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatterStampFileWriter {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private final IntraMartStampProperties properties;

    public void append(String operation, List<MatterStampData> stamps) {
        if (!properties.getFile().isEnabled() || stamps == null || stamps.isEmpty()) {
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
            for (MatterStampData stamp : stamps) {
                sb.append("systemMatterId: ").append(stamp.systemMatterId()).append('\n');
                sb.append("nodeId: ").append(stamp.nodeId()).append('\n');
                sb.append("stampType: ").append(stamp.stampType()).append('\n');
                sb.append("stampStr1: ").append(stamp.stampStr1()).append('\n');
                sb.append("processDate: ").append(stamp.processDate()).append('\n');
            }
            sb.append('\n');
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.warn("failed to append matter stamp data to {}: {}", pathValue, ex.getMessage());
        }
    }
}
