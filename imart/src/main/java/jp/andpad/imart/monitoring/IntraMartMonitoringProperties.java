package jp.andpad.imart.monitoring;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * intra-mart ワークフローモニタリング（{@code MonitoringManager}）設定。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/MonitoringManager/index.html">MonitoringManager</a>
 */
@Data
@ConfigurationProperties(prefix = "app.imart.monitoring")
public class IntraMartMonitoringProperties {

    /** モニタリング連携を有効化する。 */
    private boolean enabled = true;

    /** ワークフロー遷移時にフロー別モニタリング情報を自動更新する。 */
    private boolean recordOnTransition = true;

    /** フロー別モニタリング情報を PostgreSQL に保存する。 */
    private boolean storeInDatabase = true;

    /** スタブモードで追記するフロー別モニタリングログ。 */
    private final FileStore file = new FileStore();

    /** モニタリング対象フロー ID 一覧。空の場合は全フロー。 */
    private List<String> flowIds = List.of("document-approval");

    @Data
    public static class FileStore {
        private boolean enabled = true;
        private String path = "/app/data/monitoring/flow-data.log";
    }
}
