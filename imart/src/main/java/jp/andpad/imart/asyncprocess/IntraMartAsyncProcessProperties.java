package jp.andpad.imart.asyncprocess;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * intra-mart 非同期処理ワークフロー（{@code AsyncProcessWorkflow}）設定。
 */
@Data
@ConfigurationProperties(prefix = "app.imart.asyncprocess")
public class IntraMartAsyncProcessProperties {

    /** 非同期処理状況連携を有効化する。 */
    private boolean enabled = true;

    /** ワークフロー遷移時に非同期処理状況を自動記録する。 */
    private boolean recordOnTransition = true;

    /** 非同期処理状況を PostgreSQL に保存する。 */
    private boolean storeInDatabase = true;

    /** スタブモードで追記するログ。 */
    private final FileStore file = new FileStore();

    /** 記録対象フロー ID 一覧。空の場合は全フロー。 */
    private List<String> flowIds = List.of("document-approval", "tenant-provisioning");

    @Data
    public static class FileStore {
        private boolean enabled = true;
        private String path = "/app/data/asyncprocess/status.log";
    }
}
