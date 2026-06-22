package jp.andpad.imart.cnfmactv;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/** intra-mart 未完了案件確認一覧（{@code CnfmActvMatterList}）設定。 */
@Data
@ConfigurationProperties(prefix = "app.imart.cnfmactv")
public class IntraMartCnfmActvMatterProperties {

    private boolean enabled = true;
    private boolean storeInDatabase = true;
    private final FileStore file = new FileStore();
    private List<String> flowIds = List.of("document-approval");

    @Data
    public static class FileStore {
        private boolean enabled = true;
        private String path = "/app/data/cnfmactv/matters.log";
    }
}
