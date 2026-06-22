package jp.andpad.imart.cnfmactv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CnfmActvMatterFileWriter {

    private final IntraMartCnfmActvMatterProperties properties;

    public void append(String operation, List<ActvMatterCnfmData> models) {
        if (!properties.getFile().isEnabled()) {
            return;
        }
        try {
            Path path = Path.of(properties.getFile().getPath());
            Files.createDirectories(path.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append(Instant.now()).append('\t').append(operation).append('\n');
            for (ActvMatterCnfmData model : models) {
                sb.append("  ")
                        .append(model.listType())
                        .append(" systemMatterId=")
                        .append(model.systemMatterId())
                        .append(" matter=")
                        .append(model.matterName())
                        .append('\n');
            }
            Files.writeString(
                    path,
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.warn("failed to write cnfm actv matter log: {}", ex.getMessage());
        }
    }
}
