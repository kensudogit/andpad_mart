package jp.andpad.imart.asyncprocess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** スタブモード用非同期処理状況ログ追記。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncProcessFileWriter {

    private final IntraMartAsyncProcessProperties properties;

    public void append(String operation, List<AsyncProcessStatusData> models) {
        if (!properties.getFile().isEnabled()) {
            return;
        }
        try {
            Path path = Path.of(properties.getFile().getPath());
            Files.createDirectories(path.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append(Instant.now()).append('\t').append(operation).append('\n');
            for (AsyncProcessStatusData model : models) {
                sb.append("  acceptId=").append(model.acceptId())
                        .append(" flowId=").append(model.flowId())
                        .append(" status=").append(model.asyncProcStatus())
                        .append(" matter=").append(model.matterName())
                        .append('\n');
            }
            Files.writeString(
                    path,
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.warn("failed to write async process log: {}", ex.getMessage());
        }
    }
}
