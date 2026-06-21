package jp.andpad.imart.stamp;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * intra-mart 完了案件印影（{@code CplMatterStampList}）設定。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/im_workflow/CplMatterStampList/index.html">CplMatterStampList</a>
 */
@Data
@ConfigurationProperties(prefix = "app.imart.stamp")
public class IntraMartStampProperties {

    /** 印影連携を有効化する。 */
    private boolean enabled = true;

    /** ワークフロー完了時に印影データを自動登録する。 */
    private boolean recordOnTransition = true;

    /** 印影データを PostgreSQL に保存する。 */
    private boolean storeInDatabase = true;

    /** スタブモードで追記する印影ログ。 */
    private final FileStore file = new FileStore();

    /** 印影対象フロー ID 一覧。空の場合は全フロー。 */
    private List<String> flowIds = List.of("document-approval");

    @Data
    public static class FileStore {
        private boolean enabled = true;
        private String path = "/app/data/stamp/matter-stamps.log";
    }
}
